package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	stdnet "net"
	"net/http"
	"os"
	"runtime"
	"sort"
	"time"

	"github.com/IBM/sarama"
	"github.com/shirou/gopsutil/v3/cpu"
	// "github.com/shirou/gopsutil/v3/disk" // optional (see collectMetrics)
	"github.com/shirou/gopsutil/v3/mem"
	gnet "github.com/shirou/gopsutil/v3/net"
	"github.com/shirou/gopsutil/v3/process"
)

// ==================================================================
//  Configuration
// ==================================================================

type AgentConfig struct {
	ServerURL   string `json:"serverUrl"`
	AgentID     string `json:"agentId"`
	ApiKey      string `json:"apiKey"`
	KafkaBroker string `json:"kafkaBroker"`
	KafkaTopic  string `json:"kafkaTopic"`
}

const (
	ConfigFile        = "agent_config.json"
	HeartbeatInterval = 30 * time.Second
	DataInterval      = 10 * time.Second
)

var config AgentConfig

// ==================================================================
//  Data Models (MATCH Java TelemetryKafkaMessage)
// ==================================================================

type MetricReport struct {
	AgentID        string  `json:"agentId"`
	ApiKey         string  `json:"apiKey"`
	Hostname       string  `json:"hostname"`
	CpuUsage       float64 `json:"cpuUsage"`
	RamUsedPercent float64 `json:"ramUsedPercent"`

	RamTotalMb      uint64  `json:"ram_total_mb"`
	DiskUsedPercent float64 `json:"disk_used_percent"`
	DiskTotalGb     uint64  `json:"disk_total_gb"`

	// Process and network details
	Processes          []ProcessModel      `json:"processes"`
	NetworkConnections []NetworkConnection `json:"networkConnections"`

	// Network speed metrics
	BytesSentSec uint64    `json:"bytesSentSec"`
	BytesRecvSec uint64    `json:"bytesRecvSec"`
	Timestamp    time.Time `json:"timestamp"`
}

type ProcessModel struct {
	Pid      int32   `json:"pid"`
	Name     string  `json:"name"`
	Cpu      float64 `json:"cpu"`
	Username string  `json:"username"`
}

type NetworkConnection struct {
	Pid int32 `json:"pid"`

	LocalAddress  string `json:"local_address"`
	LocalPort     uint32 `json:"local_port"`
	RemoteAddress string `json:"remote_address"`
	RemotePort    uint32 `json:"remote_port"`
	ProcessName   string `json:"process_name"`

	Status string `json:"status"`
}

type RegistrationRequest struct {
	Hostname        string `json:"hostname"`
	OperatingSystem string `json:"operatingSystem"`
	AgentVersion    string `json:"agentVersion"`
	IpAddress       string `json:"ipAddress"`
}

type RegistrationResponse struct {
	AgentID string `json:"agentId"`
	ApiKey  string `json:"apiKey"`
	Status  string `json:"status"`
	Message string `json:"message"`
}

type HeartbeatRequest struct {
	AgentID        string  `json:"agentId"`
	CpuUsage       float64 `json:"cpuUsage"`
	RamUsedPercent float64 `json:"ramUsedPercent"`
	Status         string  `json:"status"`
}

// ==================================================================
//  Network Stats
// ==================================================================

var lastBytesSent uint64
var lastBytesRecv uint64
var lastCheckTime time.Time

func initNetworkStats() {
	lastCheckTime = time.Now()
	io, err := gnet.IOCounters(false)
	if err == nil && len(io) > 0 {
		lastBytesSent = io[0].BytesSent
		lastBytesRecv = io[0].BytesRecv
	}
}

// ==================================================================
//  Main Application
// ==================================================================

func main() {
	fmt.Println("🛡️ SentinelAgent: Secure Network Intelligence Module Starting...")

	// Load default configuration
	loadDefaultConfig()

	// Try to load existing config or register
	if !loadConfig() {
		fmt.Println("📝 No existing configuration found. Registering with server...")
		if err := registerAgent(); err != nil {
			log.Printf("⚠️ Warning: Could not register with server: %v", err)
			log.Println("📡 Continuing in standalone mode (direct Kafka)")
		} else {
			fmt.Println("✅ Agent registered successfully!")
		}
	} else {
		fmt.Printf("✅ Loaded existing configuration. Agent ID: %s\n", config.AgentID)
	}

	initNetworkStats()

	// Create Kafka producer
	kafkaConfig := sarama.NewConfig()
	kafkaConfig.Producer.Return.Successes = true

	// Recommended stability knobs
	kafkaConfig.Net.DialTimeout = 10 * time.Second
	kafkaConfig.Net.ReadTimeout = 10 * time.Second
	kafkaConfig.Net.WriteTimeout = 10 * time.Second
	kafkaConfig.Producer.Retry.Max = 5
	kafkaConfig.Producer.Retry.Backoff = 2 * time.Second

	producer, err := sarama.NewSyncProducer([]string{config.KafkaBroker}, kafkaConfig)
	if err != nil {
		log.Fatalf("❌ Failed to start Kafka producer: %v", err)
	}
	defer producer.Close()

	fmt.Printf("📡 Connected to Kafka at %s\n", config.KafkaBroker)
	fmt.Println("🔄 Monitoring System & Network...")

	// Start heartbeat goroutine if registered
	if config.AgentID != "" && config.ApiKey != "" {
		go heartbeatLoop()
	}

	// Main data collection loop
	for {
		report := collectMetrics()

		jsonBytes, err := json.Marshal(report)
		if err != nil {
			log.Println("Error marshalling JSON:", err)
			time.Sleep(DataInterval)
			continue
		}

		msg := &sarama.ProducerMessage{
			Topic: config.KafkaTopic,
			Value: sarama.ByteEncoder(jsonBytes),
		}

		// Add agent metadata as headers if registered
		if config.AgentID != "" {
			msg.Headers = []sarama.RecordHeader{
				{Key: []byte("X-Agent-ID"), Value: []byte(config.AgentID)},
				{Key: []byte("X-Agent-Key"), Value: []byte(config.ApiKey)},
			}
		}

		_, _, err = producer.SendMessage(msg)
		if err != nil {
			log.Printf("❌ Kafka Error: %v", err)
		} else {
			fmt.Printf(
				"✅ Sent: [CPU: %.1f%% | Up: %d KB/s | Down: %d KB/s | Conns: %d]\n",
				report.CpuUsage,
				report.BytesSentSec/1024,
				report.BytesRecvSec/1024,
				len(report.NetworkConnections),
			)
		}

		time.Sleep(DataInterval)
	}
}

// ==================================================================
//  Configuration Management
// ==================================================================

func loadDefaultConfig() {
	config = AgentConfig{
		ServerURL:   getEnv("SENTINEL_SERVER", "http://localhost:8080"),
		KafkaBroker: getEnv("KAFKA_BROKER", "127.0.0.1:9092"),
		KafkaTopic:  getEnv("KAFKA_TOPIC", "agent-data"),
	}
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func loadConfig() bool {
	data, err := ioutil.ReadFile(ConfigFile)
	if err != nil {
		return false
	}

	var savedConfig AgentConfig
	if err := json.Unmarshal(data, &savedConfig); err != nil {
		return false
	}

	// Merge saved config with defaults (prioritize saved values)
	if savedConfig.AgentID != "" {
		config.AgentID = savedConfig.AgentID
	}
	if savedConfig.ApiKey != "" {
		config.ApiKey = savedConfig.ApiKey
	}
	if savedConfig.ServerURL != "" {
		config.ServerURL = savedConfig.ServerURL
	}
	if savedConfig.KafkaBroker != "" {
		config.KafkaBroker = savedConfig.KafkaBroker
	}
	if savedConfig.KafkaTopic != "" {
		config.KafkaTopic = savedConfig.KafkaTopic
	}

	return config.AgentID != "" && config.ApiKey != ""
}

func saveConfig() error {
	data, err := json.MarshalIndent(config, "", "  ")
	if err != nil {
		return err
	}
	return ioutil.WriteFile(ConfigFile, data, 0600) // Secure permissions
}

// ==================================================================
//  Agent Registration
// ==================================================================

func registerAgent() error {
	hostname, _ := os.Hostname()

	payload := RegistrationRequest{
		Hostname:        hostname,
		OperatingSystem: runtime.GOOS,
		AgentVersion:    "1.0.0",
		IpAddress:       getLocalIP(),
	}

	jsonData, _ := json.Marshal(payload)

	resp, err := http.Post(
		config.ServerURL+"/api/v1/agents/register",
		"application/json",
		bytes.NewBuffer(jsonData),
	)
	if err != nil {
		return fmt.Errorf("HTTP request failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		body, _ := ioutil.ReadAll(resp.Body)
		return fmt.Errorf("registration failed with status %d: %s", resp.StatusCode, string(body))
	}

	var result RegistrationResponse
	if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	config.AgentID = result.AgentID
	config.ApiKey = result.ApiKey

	if err := saveConfig(); err != nil {
		log.Printf("⚠️ Warning: Could not save config: %v", err)
	}

	fmt.Printf("📋 Agent ID: %s\n", config.AgentID)
	fmt.Println("🔑 API Key saved securely to config file")

	return nil
}

// Better local IP discovery (stdlib)
func getLocalIP() string {
	addrs, err := stdnet.InterfaceAddrs()
	if err != nil {
		return "unknown"
	}
	for _, addr := range addrs {
		// Example: "192.168.1.10/24"
		ip := addr.String()
		if ip == "" {
			continue
		}
		// filter loopback (IPv4/IPv6)
		if ip == "127.0.0.1" || ip == "::1" {
			continue
		}
		// Keep it simple: return first non-loopback
		return ip
	}
	return "unknown"
}

// ==================================================================
//  Heartbeat
// ==================================================================

func heartbeatLoop() {
	for {
		time.Sleep(HeartbeatInterval)

		cpuPercent, _ := cpu.Percent(time.Second, false)
		cpuVal := 0.0
		if len(cpuPercent) > 0 {
			cpuVal = cpuPercent[0]
		}
		vMem, _ := mem.VirtualMemory()

		payload := HeartbeatRequest{
			AgentID:        config.AgentID,
			CpuUsage:       cpuVal,
			RamUsedPercent: vMem.UsedPercent,
			Status:         "ACTIVE",
		}

		jsonData, _ := json.Marshal(payload)

		req, _ := http.NewRequest("POST",
			config.ServerURL+"/api/v1/agents/heartbeat",
			bytes.NewBuffer(jsonData))

		req.Header.Set("Content-Type", "application/json")
		req.Header.Set("X-Agent-Key", config.ApiKey)

		client := &http.Client{Timeout: 10 * time.Second}
		resp, err := client.Do(req)
		if err != nil {
			log.Printf("⚠️ Heartbeat failed: %v", err)
			continue
		}
		resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			log.Printf("⚠️ Heartbeat returned status: %d", resp.StatusCode)
		}
	}
}

// ==================================================================
//  Metrics Collection
// ==================================================================

func collectMetrics() MetricReport {
	hostname, _ := os.Hostname()

	// 1. CPU & RAM
	cpuPercent, _ := cpu.Percent(time.Second, false)
	cpuVal := 0.0
	if len(cpuPercent) > 0 {
		cpuVal = cpuPercent[0]
	}
	vMem, _ := mem.VirtualMemory()

	ramTotalMb := uint64(0)
	ramUsedPercent := 0.0
	if vMem != nil {
		ramTotalMb = vMem.Total / (1024 * 1024)
		ramUsedPercent = vMem.UsedPercent
	}

	// Disk
	diskUsedPercent := 0.0
	diskTotalGb := uint64(0)

	// solution to enable disk metrics:
	// usage, err := disk.Usage(os.Getenv("SystemDrive") + "\\")
	// if err == nil && usage != nil {
	// 	diskUsedPercent = usage.UsedPercent
	// 	diskTotalGb = usage.Total / (1024 * 1024 * 1024)
	// }

	// Processes (Top 5)
	procs := getTopProcesses(5)

	// Network (Smart Connections)
	conns := getSmartNetworkConnections(procs)

	// Network Speed Calculation
	bytesSentSec, bytesRecvSec := calculateNetworkSpeed()

	return MetricReport{
		AgentID:            config.AgentID,
		ApiKey:             config.ApiKey,
		Hostname:           hostname,
		CpuUsage:           cpuVal,
		RamUsedPercent:     ramUsedPercent,
		RamTotalMb:         ramTotalMb,
		DiskUsedPercent:    diskUsedPercent,
		DiskTotalGb:        diskTotalGb,
		Processes:          procs,
		NetworkConnections: conns,
		BytesSentSec:       bytesSentSec,
		BytesRecvSec:       bytesRecvSec,
		Timestamp:          time.Now(),
	}
}

func calculateNetworkSpeed() (uint64, uint64) {
	io, err := gnet.IOCounters(false)
	if err != nil || len(io) == 0 {
		return 0, 0
	}

	currentSent := io[0].BytesSent
	currentRecv := io[0].BytesRecv
	now := time.Now()

	duration := now.Sub(lastCheckTime).Seconds()
	if duration == 0 {
		duration = 1
	}

	sentSpeed := uint64(float64(currentSent-lastBytesSent) / duration)
	recvSpeed := uint64(float64(currentRecv-lastBytesRecv) / duration)

	lastBytesSent = currentSent
	lastBytesRecv = currentRecv
	lastCheckTime = now

	return sentSpeed, recvSpeed
}

func getTopProcesses(limit int) []ProcessModel {
	ps, _ := process.Processes()
	var models []ProcessModel

	for _, p := range ps {
		cpuP, _ := p.CPUPercent()
		if cpuP > 0.1 {
			name, _ := p.Name()
			user, _ := p.Username()
			models = append(models, ProcessModel{
				Pid: p.Pid, Name: name, Cpu: cpuP, Username: user,
			})
		}
	}

	sort.Slice(models, func(i, j int) bool { return models[i].Cpu > models[j].Cpu })
	if len(models) > limit {
		return models[:limit]
	}
	return models
}

func getSmartNetworkConnections(topProcs []ProcessModel) []NetworkConnection {
	connections, _ := gnet.Connections("inet")
	var models []NetworkConnection

	procNames := make(map[int32]string)
	for _, p := range topProcs {
		procNames[p.Pid] = p.Name
	}

	count := 0
	for _, c := range connections {
		// keep only meaningful connections
		if c.Status == "ESTABLISHED" && c.Raddr.IP != "" && c.Raddr.IP != "127.0.0.1" && c.Raddr.IP != "::1" {

			pName := procNames[c.Pid]
			if pName == "" {
				if p, err := process.NewProcess(c.Pid); err == nil {
					pName, _ = p.Name()
				}
			}

			models = append(models, NetworkConnection{
				Pid:           c.Pid,
				ProcessName:   pName,
				LocalAddress:  c.Laddr.IP,
				LocalPort:     c.Laddr.Port,
				RemoteAddress: c.Raddr.IP,
				RemotePort:    c.Raddr.Port,
				Status:        c.Status,
			})

			count++
			if count >= 8 {
				break
			}
		}
	}

	return models
}
