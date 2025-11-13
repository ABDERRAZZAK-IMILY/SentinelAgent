#  SentinelAgent – Cyber Attack Detector (Go + Spring Boot + Spring AI + Angular)

This document describes the architecture and main components of the **SentinelAgent** system — a distributed cyber-attack detection platform composed of a Go-based agent, Spring Boot backend, Spring AI engine, and Angular dashboard.

---

##  System Architecture Overview

```text
+------------------------------------------------------+
|                     Agent (Go)                       |
+------------------------------------------------------+
| - id: String                                         |
| - hostname: String                                   |
| - osVersion: String                                  |
| - ipAddress: String                                  |
| - status: AgentStatus (enum: ACTIVE, INACTIVE)       |
| - lastHeartbeat: LocalDateTime                       |
+------------------------------------------------------+
| + collectSystemMetrics(): Metrics                    |
| + collectNetworkData(): NetworkInfo                  |
| + sendReport(): void                                 |
| + receiveCommand(cmd: Command): void                 |
+------------------------------------------------------+

                          │ 1..*
                          │
                          ▼
+------------------------------------------------------+
|                MetricsReport (Go → API)              |
+------------------------------------------------------+
| - id: Long                                           |
| - cpuUsage: double                                   |
| - memoryUsage: double                                |
| - networkTraffic: double                             |
| - processList: List<ProcessInfo>                     |
| - timestamp: LocalDateTime                           |
| - agentId: String                                    |
+------------------------------------------------------+

                          │
                          ▼
+------------------------------------------------------+
|                ThreatAnalysisEngine (Spring AI)      |
+------------------------------------------------------+
| - id: Long                                           |
| - modelVersion: String                               |
| - lastTrainingDate: LocalDate                        |
| - detectionThreshold: double                         |
+------------------------------------------------------+
| + analyze(report: MetricsReport): ThreatAlert         |
| + trainModel(dataset: List<MetricsReport>): void      |
| + evaluateAccuracy(): double                          |
+------------------------------------------------------+

                          │
                          ▼
+------------------------------------------------------+
|                    ThreatAlert                       |
+------------------------------------------------------+
| - id: Long                                           |
| - severity: SeverityLevel (LOW, MEDIUM, HIGH, CRITICAL) |
| - message: String                                    |
| - sourceAgent: Agent                                 |
| - timestamp: LocalDateTime                           |
| - status: AlertStatus (NEW, REVIEWED, RESOLVED)      |
+------------------------------------------------------+
| + markAsReviewed(): void                             |
| + resolve(): void                                    |
+------------------------------------------------------+

                          │
                          ▼
+------------------------------------------------------+
|                      User                            |
+------------------------------------------------------+
| - id: Long                                           |
| - username: String                                   |
| - password: String                                   |
| - role: Role (ADMIN, ANALYST)                        |
| - email: String                                      |
+------------------------------------------------------+
| + login(): boolean                                   |
| + viewAlerts(): List<ThreatAlert>                    |
| + manageAgents(): void                               |
+------------------------------------------------------+

                          │
                          ▼
+------------------------------------------------------+
|                   DashboardService                   |
+------------------------------------------------------+
| + getActiveAgents(): List<Agent>                     |
| + getAlerts(): List<ThreatAlert>                     |
| + getStatistics(): SystemStats                       |
| + sendCommandToAgent(agentId, cmd): void             |
+------------------------------------------------------+

```

# Tech Stack
```
+----------------+----------------------+---------------------------------------------------+
| Layer          | Technology           | Description                                       |
+----------------+----------------------+---------------------------------------------------+
| Agent          | Go                   | Collects system/network metrics from Windows PCs  |
| Backend API    | Spring Boot          | Manages agents, users, and incoming reports       |
| AI Engine      | Spring AI            | Analyzes metrics and generates threat alerts      |
| Dashboard      | Angular              | Displays real-time alerts, stats, and agent data  |
+----------------+----------------------+---------------------------------------------------+
```