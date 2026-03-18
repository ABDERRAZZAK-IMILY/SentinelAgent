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
            -w /workspace/backend \
            maven:3.9.9-eclipse-temurin-17 \
            sh -lc 'mvn --batch-mode clean verify'
        '''
      }
    }

    stage('Frontend - Install & Build') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            -w /workspace/frontend \
            node:20-bookworm \
            sh -lc 'npm ci && npm run build'
        '''
      }
    }

    stage('Go Agent - Build') {
      steps {
        sh '''
          docker run --rm \
            -v "$PWD":/workspace \
            -w /workspace/agentTopic \
            golang:1.25-bookworm \
            sh -lc 'go mod download && mkdir -p bin && go build -o bin/sentinel-agent ./agent.go'
        '''
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
      archiveArtifacts allowEmptyArchive: true, artifacts: 'backend/target/*.jar,frontend/dist/**,agentTopic/bin/**'
    }
  }
}
