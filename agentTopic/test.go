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
}

type ProcessModel struct {
	Pid      int32   `json:"pid"`
	Name     string  `json:"name"`
	CpuUsage float64 `json:"cpuUsage"`
	Username string  `json:"username"`
}

type NetworkConnection struct {
	Pid           int32  `json:"pid"`
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

func main() {
	fmt.Println(" Starting SentinelAgent (Optimized for AI)...")

	config := sarama.NewConfig()
	config.Producer.Return.Successes = true

	producer, err := sarama.NewSyncProducer([]string{KafkaBroker}, config)
	if err != nil {
		log.Fatalf(" Failed to start Kafka producer: %v", err)
	}
	defer producer.Close()

	fmt.Println(" Connected to Kafka. Sending metrics every 10 seconds...")

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
			log.Printf(" Failed to send message: %v", err)
		} else {
			fmt.Printf(" Data Sent! [CPU: %.1f%% | RAM: %.1f%% | Active Procs: %d]\n",
				report.CpuUsage, report.RamUsedPercent, len(report.Processes))
		}

		time.Sleep(10 * time.Second)
	}
}

func collectMetrics() MetricReport {
	// CPU
	cpuPercent, _ := cpu.Percent(time.Second, false)
	cpuVal := 0.0
	if len(cpuPercent) > 0 {
		cpuVal = cpuPercent[0]
	}

	// RAM
	vMem, _ := mem.VirtualMemory()

	procs := getTopProcesses(5)

	conns := getNetworkConnections()

	return MetricReport{
		CpuUsage:       cpuVal,
		RamUsedPercent: vMem.UsedPercent,
		Processes:      procs,
		Network:        conns,
	}
}

func getTopProcesses(limit int) []ProcessModel {
	ps, _ := process.Processes()
	var models []ProcessModel

	for _, p := range ps {
		name, _ := p.Name()
		cpuP, _ := p.CPUPercent()
		user, _ := p.Username()

		if cpuP > 0.1 {
			models = append(models, ProcessModel{
				Pid:      p.Pid,
				Name:     name,
				CpuUsage: cpuP,
				Username: user,
			})
		}
	}

	sort.Slice(models, func(i, j int) bool {
		return models[i].CpuUsage > models[j].CpuUsage
	})

	if len(models) > limit {
		return models[:limit]
	}
	return models
}

func getNetworkConnections() []NetworkConnection {
	connections, _ := net.Connections("inet")
	var models []NetworkConnection

	count := 0
	for _, c := range connections {
		if c.Status == "ESTABLISHED" || c.Status == "LISTEN" {
			models = append(models, NetworkConnection{
				Pid:           c.Pid,
				LocalAddress:  c.Laddr.IP,
				LocalPort:     c.Laddr.Port,
				RemoteAddress: c.Raddr.IP,
				RemotePort:    c.Raddr.Port,
				Status:        c.Status,
			})
			count++
			if count >= 5 {
				break
			}
		}
	}
	return models
}
