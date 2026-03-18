pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  environment {
    CI = 'true'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Backend - Build & Test') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            maven:3.9.9-eclipse-temurin-17 \
            sh -lc '
              set -e
              BACKEND_DIR="$(find /workspace -maxdepth 5 -type f -path "*/backend/pom.xml" | head -n 1 | xargs -r dirname)"
              if [ -z "$BACKEND_DIR" ]; then
                echo "backend/pom.xml not found under /workspace"
                find /workspace -maxdepth 5 -type f -name pom.xml -print
                exit 1
              fi
              cd "$BACKEND_DIR"
              mvn --batch-mode clean verify
            '
        '''
      }
    }

    stage('Frontend - Install & Build') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            node:20-bookworm \
            sh -lc '
              set -e
              FRONTEND_DIR="$(find /workspace -maxdepth 5 -type f -path "*/frontend/package.json" | head -n 1 | xargs -r dirname)"
              if [ -z "$FRONTEND_DIR" ]; then
                echo "frontend/package.json not found under /workspace"
                find /workspace -maxdepth 5 -type f -name package.json -print
                exit 1
              fi
              cd "$FRONTEND_DIR"
              npm ci
              npm run build
            '
        '''
      }
    }

    stage('Go Agent - Build') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            golang:1.25-bookworm \
            sh -lc '
              set -e
              AGENT_DIR="$(find /workspace -maxdepth 5 -type f -path "*/agentTopic/go.mod" | head -n 1 | xargs -r dirname)"
              if [ -z "$AGENT_DIR" ]; then
                echo "agentTopic/go.mod not found under /workspace"
                find /workspace -maxdepth 5 -type f -name go.mod -print
                exit 1
              fi
              cd "$AGENT_DIR"
              go mod download
              mkdir -p bin
              go build -o bin/sentinel-agent ./agent.go
            '
        '''
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: '**/backend/target/surefire-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true, artifacts: '**/backend/target/*.jar,**/frontend/dist/**,**/agentTopic/bin/**'
    }
  }
}
