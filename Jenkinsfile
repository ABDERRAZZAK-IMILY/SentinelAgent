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
          set -e
          if [ ! -f "$WORKSPACE/backend/pom.xml" ]; then
            echo "Missing backend/pom.xml in workspace: $WORKSPACE"
            find "$WORKSPACE" -maxdepth 5 -type f -name pom.xml -print || true
            exit 1
          fi

          docker run --rm \
            -v "$WORKSPACE/backend":/workspace \
            -w /workspace \
            maven:3.9.9-eclipse-temurin-17 \
            sh -lc '
              set -e
              mvn --batch-mode clean verify
            '
        '''
      }
    }

    stage('Frontend - Install & Build') {
      steps {
        sh '''
          set -e
          if [ ! -f "$WORKSPACE/frontend/package.json" ]; then
            echo "Missing frontend/package.json in workspace: $WORKSPACE"
            find "$WORKSPACE" -maxdepth 5 -type f -name package.json -print || true
            exit 1
          fi

          docker run --rm \
            -v "$WORKSPACE/frontend":/workspace \
            -w /workspace \
            node:20-bookworm \
            sh -lc '
              set -e
              npm ci
              npm run build
            '
        '''
      }
    }

    stage('Go Agent - Build') {
      steps {
        sh '''
          set -e
          if [ ! -f "$WORKSPACE/agentTopic/go.mod" ]; then
            echo "Missing agentTopic/go.mod in workspace: $WORKSPACE"
            find "$WORKSPACE" -maxdepth 5 -type f -name go.mod -print || true
            exit 1
          fi

          docker run --rm \
            -v "$WORKSPACE/agentTopic":/workspace \
            -w /workspace \
            golang:1.25-bookworm \
            sh -lc '
              set -e
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
