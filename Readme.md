# SentinelAgent

SentinelAgent is a modular cybersecurity monitoring platform designed for SOC-style workflows. It combines endpoint telemetry collection, backend analysis, AI-assisted threat enrichment, and a web dashboard for investigation and reporting.

Status: active development.

## Repository

- GitHub: https://github.com/ABDERRAZZAK-IMILY/SentinelAgent

## Author

- Abderrazzak Imily

## Key Capabilities

- Endpoint telemetry collection from a Go-based agent
- Streaming and ingestion pipeline using Kafka
- Backend analysis and API services with Spring Boot
- AI-assisted enrichment using Spring AI and vector search (Qdrant)
- SOC-oriented frontend dashboard built with Angular
- CI pipeline for backend, frontend, and agent builds via Jenkins

## Architecture Overview

SentinelAgent is organized into three main runtime components and supporting infrastructure:

- `agentTopic/`: Go agent that collects host/network/process metrics and sends telemetry to Kafka
- `backend/`: Spring Boot API that consumes telemetry, handles security analysis logic, and integrates with AI/vector services
- `frontend/`: Angular application for alerts, telemetry views, and security operations workflows
- `jenkins/`: Jenkins container setup for CI orchestration

High-level flow:

1. Agent collects telemetry from monitored hosts.
2. Telemetry is published to Kafka.
3. Backend consumes and processes events, stores data in MongoDB, and enriches analysis with Spring AI + Qdrant.
4. Frontend consumes backend APIs to present dashboards, alerts, and operational views.

## Technology Stack

- Backend: Java 17, Spring Boot 3.5, Spring Security, Spring Kafka, Spring Data MongoDB, Spring Modulith, Spring AI
- Frontend: Angular 19, TypeScript, NgRx, Tailwind CSS, DaisyUI
- Agent: Go 1.25, Sarama (Kafka), gopsutil
- Infrastructure: Kafka, Zookeeper, MongoDB, Qdrant, Docker Compose, Jenkins

## Project Structure

```text
SentinelAgent/
|- backend/       # Spring Boot API and analysis services
|- frontend/      # Angular dashboard
|- agentTopic/    # Go telemetry agent + local infra compose
|- jenkins/       # Jenkins runtime compose
|- Jenkinsfile    # CI pipeline definition
```

## Prerequisites

Install the following tools locally:

- Java 17+
- Maven 3.9+ (or use `backend/mvnw`)
- Node.js 20+ and npm
- Go 1.25+
- Docker + Docker Compose

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/ABDERRAZZAK-IMILY/SentinelAgent.git
cd SentinelAgent
```

### 2. Start infrastructure services

From `agentTopic/`, start local dependencies (Kafka, Zookeeper, MongoDB, Qdrant, mongo-express):

```bash
cd agentTopic
docker compose up -d
cd ..
```

### 3. Run backend

```bash
cd backend
./mvnw spring-boot:run
```

Default backend port: `8080`.

### 4. Run frontend

```bash
cd frontend
npm ci
npm start
```

Default frontend dev server: Angular CLI (`ng serve`, usually port `4200`).

### 5. Run Go agent

```bash
cd agentTopic
go mod tidy
go run agent.go
```

The agent reads/writes local configuration in `agentTopic/agent_config.json` and publishes telemetry to Kafka.

## Configuration

Primary backend configuration is in:

- `backend/src/main/resources/application.properties`

Important settings include:

- `server.port` (default `8080`)
- MongoDB connection (`spring.data.mongodb.uri`)
- Kafka consumer settings (`spring.kafka.*`)
- Qdrant host/port (`spring.ai.vectorstore.qdrant.*`)
- LLM provider credentials (`spring.ai.openai.*`)
- JWT settings (`jwt.*`)

Environment-variable based secrets supported by default include:

- `OPENAI_API_KEY`
- `ABUSEIPDB_API_KEY`

## CI Pipeline

The root `Jenkinsfile` defines a multi-stage pipeline:

1. Checkout
2. Backend build and tests (`mvn clean verify`)
3. Frontend install and build (`npm ci`, `npm run build`)
4. Go agent build (`go build`)
5. Archive artifacts and publish JUnit reports

A Jenkins runtime container is provided in `jenkins/docker-compose.yml`.

## Autre 

IMILY abderrazzak