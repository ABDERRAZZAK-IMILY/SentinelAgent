pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  stages {
    stage('Checkout') {
      steps {
        deleteDir()
        checkout scm
      }
    }

    stage('Backend - Build & Test') {
      steps {
        sh '''
          set -e
          rm -rf "$WORKSPACE/backend/target" "$WORKSPACE/backend/target/classes"
          LEGACY_AUTH_DIR="$WORKSPACE/backend/src/main/java/com/sentinelagent/backend/auth"
          if [ -d "$LEGACY_AUTH_DIR" ]; then
            find "$LEGACY_AUTH_DIR" -maxdepth 1 -type f -name '*.java' -print -delete || true
          fi

          docker build --pull \
            -t sentinel-backend-ci:${BUILD_NUMBER} \
            -f - "$WORKSPACE/backend" <<'EOF'
FROM maven:3.9.9-eclipse-temurin-17
WORKDIR /workspace
COPY . .
RUN mvn --batch-mode clean verify -Dtest=!BackendApplicationTests
EOF

          CID="$(docker create sentinel-backend-ci:${BUILD_NUMBER})"
          rm -rf "$WORKSPACE/backend/target"
          docker cp "$CID:/workspace/target" "$WORKSPACE/backend/"
          docker rm -f "$CID" >/dev/null
        '''
      }
    }

    stage('Frontend - Install & Build') {
      steps {
        sh '''
          set -e
          docker build --pull \
            -t sentinel-frontend-ci:${BUILD_NUMBER} \
            -f - "$WORKSPACE/frontend" <<'EOF'
FROM node:20-bookworm
WORKDIR /workspace
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
EOF

          CID="$(docker create sentinel-frontend-ci:${BUILD_NUMBER})"
          rm -rf "$WORKSPACE/frontend/dist"
          docker cp "$CID:/workspace/dist" "$WORKSPACE/frontend/"
          docker rm -f "$CID" >/dev/null
        '''
      }
    }

    stage('Go Agent - Build') {
      steps {
        sh '''
          set -e
          docker build --pull \
            -t sentinel-agent-ci:${BUILD_NUMBER} \
            -f - "$WORKSPACE/agentTopic" <<'EOF'
FROM golang:1.25-bookworm
WORKDIR /workspace
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN mkdir -p bin && go build -o bin/sentinel-agent ./agent.go
EOF

          CID="$(docker create sentinel-agent-ci:${BUILD_NUMBER})"
          rm -rf "$WORKSPACE/agentTopic/bin"
          docker cp "$CID:/workspace/bin" "$WORKSPACE/agentTopic/"
          docker rm -f "$CID" >/dev/null
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
