package main

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"os"
	"time"

	"github.com/segmentio/kafka-go"
	"github.com/shirou/gopsutil/v3/cpu"
	"github.com/shirou/gopsutil/v3/disk"
	"github.com/shirou/gopsutil/v3/mem"
	"github.com/shirou/gopsutil/v3/process"
	"github.com/shirou/gopsutil/v3/net"
)

var writer *kafka.Writer

type ProcessInfo struct {
	PID      int32   `json:"pid"`
	Name     string  `json:"name"`
	CPU      float64 `json:"cpu"`
	Username string  `json:"username"`
}

type NetworkConnectionInfo struct {
	PID          int32  `json:"pid"`
	LocalAddress string `json:"local_address"`
	LocalPort    uint32 `json:"local_port"`
	RemoteAddress string `json:"remote_address"`
	RemotePort   uint32 `json:"remote_port"`
	Status       string `json:"status"`
}

func main() {
	f, err := os.OpenFile("sentinelagent.log", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		log.Fatal(err)
	}
	defer f.Close()
	log.SetOutput(f)

	err = createTopic("127.0.0.1:9092", "agent-data")
	if err != nil {
		log.Println(" Failed to create topic:", err)
	} else {
		log.Println(" Topic 'agent-data' ready.")
	}

	writer = kafka.NewWriter(kafka.WriterConfig{
		Brokers: []string{"127.0.0.1:9092"},
		Topic:   "agent-data",
	})

	log.Println(" Go Agent started. Sending data every 10 seconds...")

	for {
		sendSystemMetrics()
		time.Sleep(10 * time.Second)
	}
}

func createTopic(brokerAddr, topic string) error {
	conn, err := kafka.Dial("tcp", brokerAddr)
	if err != nil {
		return fmt.Errorf("failed to connect to Kafka broker: %v", err)
	}
	defer conn.Close()

	controller, err := conn.Controller()
	if err != nil {
		return fmt.Errorf("failed to get controller: %v", err)
	}

	controllerConn, err := kafka.Dial("tcp", fmt.Sprintf("%s:%d", controller.Host, controller.Port))
	if err != nil {
		return fmt.Errorf("failed to connect to controller: %v", err)
	}
	defer controllerConn.Close()

	topicConfigs := []kafka.TopicConfig{
		{
			Topic:             topic,
			NumPartitions:     1,
			ReplicationFactor: 1,
		},
	}

	err = controllerConn.CreateTopics(topicConfigs...)
	if err != nil {
		return fmt.Errorf("failed to create topic: %v", err)
	}
	return nil
}

func sendSystemMetrics() {
	data := make(map[string]interface{})

	cpuPercents, err := cpu.Percent(0, false)
	if err == nil && len(cpuPercents) > 0 {
		data["cpu"] = cpuPercents[0]
	}

	vmStat, err := mem.VirtualMemory()
	if err == nil {
		data["ram_used_percent"] = vmStat.UsedPercent
		data["ram_total_mb"] = vmStat.Total / 1024 / 1024
	}

	diskStat, err := disk.Usage("C:")
	if err == nil {
		data["disk_used_percent"] = diskStat.UsedPercent
		data["disk_total_gb"] = diskStat.Total / 1024 / 1024 / 1024
	}

	procList := []ProcessInfo{}


	

	processes, err := process.Processes()
	if err != nil {
		log.Println(" Error getting processes:", err)
	} else {
		for _, p := range processes {
			name, errName := p.Name()
			if errName != nil {
				name = "N/A" // N/A = Not Available
			}

			cpuPercent, errCPU := p.CPUPercent()
			if errCPU != nil {
				cpuPercent = 0.0
			}

			username, errUser := p.Username()
			if errUser != nil {
				username = "N/A"
			}

			procList = append(procList, ProcessInfo{
				PID:      p.Pid,
				Name:     name,
				CPU:      cpuPercent,
				Username: username,
			})
		}
	}

	data["processes"] = procList


		netList := []NetworkConnectionInfo{}


	connections, err := net.Connections("tcp")
	if err != nil {
		log.Println(" Error getting network connections:", err)
	} else {
		for _, c := range connections {
			if c.Raddr.IP != "" {
				netList = append(netList, NetworkConnectionInfo{
					PID:          c.Pid,
					LocalAddress: c.Laddr.IP,
					LocalPort:    c.Laddr.Port,
					RemoteAddress: c.Raddr.IP,
					RemotePort:   c.Raddr.Port,
					Status:       c.Status,
				})
			}
		}
	}
	data["network_connections"] = netList



	msgBytes, err := json.Marshal(data)
	if err != nil {
		log.Println(" Error marshaling data:", err)
		return
	}

	err = writer.WriteMessages(context.Background(),
		kafka.Message{
			Key:   []byte(fmt.Sprintf("agent-%d", time.Now().Unix())),
			Value: msgBytes,
		})
	if err != nil {
		log.Println(" Error writing to Kafka:", err)
		return
	}

	log.Println(" Sent data to Kafka (with processes):", string(msgBytes))
}
