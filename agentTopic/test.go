package main

import (
	"encoding/json"
	"fmt"
	"log"
	"sort"
	"time"

	"github.com/IBM/sarama"
	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/mem"
	"github.com/shirou/gopsutil/v3/net"
	"github.com/shirou/gopsutil/v3/process"
)

type MetricReport struct {
	CpuUsage       float64             `json:"cpuUsage"`
	RamUsedPercent float64             `json:"ramUsedPercent"`
	Processes      []ProcessModel      `json:"processes"`
	Network        []NetworkConnection `json:"networkConnections"`
	BytesSentSec   uint64              `json:"bytesSentSec"`
	BytesRecvSec   uint64              `json:"bytesRecvSec"`
}

type ProcessModel struct {
	Pid      int32   `json:"pid"`
	Name     string  `json:"name"`
	CpuUsage float64 `json:"cpuUsage"`
	Username string  `json:"username"`
}

type NetworkConnection struct {
	Pid           int32  `json:"pid"`
	ProcessName   string `json:"processName"`
	LocalAddress  string `json:"localAddress"`
	LocalPort     uint32 `json:"localPort"`
	RemoteAddress string `json:"remoteAddress"`
	RemotePort    uint32 `json:"remotePort"`
	Status        string `json:"status"`
}

const (
	KafkaBroker = "localhost:9092"
	KafkaTopic  = "agent-data"
)

var lastBytesSent uint64
var lastBytesRecv uint64
var lastCheckTime time.Time

func main() {
	fmt.Println(" SentinelAgent: Network Intelligence Module Started...")

	initNetworkStats()

	config := sarama.NewConfig()
	config.Producer.Return.Successes = true
	producer, err := sarama.NewSyncProducer([]string{KafkaBroker}, config)
	if err != nil {
		log.Fatalf(" Failed to start Kafka producer: %v", err)
	}
	defer producer.Close()

	fmt.Println(" Connected to Kafka. Monitoring System & Network...")

	for {
		report := collectMetrics()

		jsonBytes, err := json.Marshal(report)
		if err != nil {
			log.Println("Error marshalling JSON:", err)
			continue
		}

		msg := &sarama.ProducerMessage{
			Topic: KafkaTopic,
			Value: sarama.StringEncoder(jsonBytes),
		}

		_, _, err = producer.SendMessage(msg)
		if err != nil {
			log.Printf(" Kafka Error: %v", err)
		} else {
			fmt.Printf(" Sent: [CPU: %.1f%% | Net Upload: %d KB/s | Active Conns: %d]\n",
				report.CpuUsage, report.BytesSentSec/1024, len(report.Network))
		}

		time.Sleep(10 * time.Second)
	}
}

func initNetworkStats() {
	lastCheckTime = time.Now()
	io, err := net.IOCounters(false)
	if err == nil && len(io) > 0 {
		lastBytesSent = io[0].BytesSent
		lastBytesRecv = io[0].BytesRecv
	}
}

func collectMetrics() MetricReport {
	// 1. CPU & RAM
	cpuPercent, _ := cpu.Percent(time.Second, false)
	cpuVal := 0.0
	if len(cpuPercent) > 0 {
		cpuVal = cpuPercent[0]
	}
	vMem, _ := mem.VirtualMemory()

	// 2. Processes (Top 5)
	procs := getTopProcesses(5)

	// 3. Network (Smart Connections)
	conns := getSmartNetworkConnections(procs)

	// 4. Network Speed Calculation
	bytesSentSec, bytesRecvSec := calculateNetworkSpeed()

	return MetricReport{
		CpuUsage:       cpuVal,
		RamUsedPercent: vMem.UsedPercent,
		Processes:      procs,
		Network:        conns,
		BytesSentSec:   bytesSentSec,
		BytesRecvSec:   bytesRecvSec,
	}
}

func calculateNetworkSpeed() (uint64, uint64) {
	io, err := net.IOCounters(false)
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
				Pid: p.Pid, Name: name, CpuUsage: cpuP, Username: user,
			})
		}
	}
	sort.Slice(models, func(i, j int) bool { return models[i].CpuUsage > models[j].CpuUsage })
	if len(models) > limit {
		return models[:limit]
	}
	return models
}

func getSmartNetworkConnections(topProcs []ProcessModel) []NetworkConnection {
	connections, _ := net.Connections("inet")
	var models []NetworkConnection

	procNames := make(map[int32]string)
	for _, p := range topProcs {
		procNames[p.Pid] = p.Name
	}

	count := 0
	for _, c := range connections {
		if c.Status == "ESTABLISHED" && c.Raddr.IP != "127.0.0.1" {

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
