package com.sentinelagent.backend.service;

import com.sentinelagent.backend.model.MetricReport;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AISecurityAnalyst {

    private final ChatClient chatClient;
    private final RagSecurityService ragService;

    public AISecurityAnalyst(ChatClient chatClient, RagSecurityService ragService) {
        this.chatClient = chatClient;
        this.ragService = ragService;
    }

    public String analyzeRisk(MetricReport report) {

        String ragContext = ragService.findMitigationStrategy("Suspicious activity with high resource usage");

        String promptText = """
                You are an advanced Cybersecurity AI Agent powered by DeepSeek.
                Your task is to analyze system metrics and detect potential threats (Malware, Ransomware, Intrusion).
                
                Context from Knowledge Base (MITRE ATT&CK):
                {rag_context}
                
                Current System Metrics:
                - CPU Usage: {cpu}%
                - RAM Usage: {ram}%
                - Active Processes: {processes}
                - Network Connections: {network}
                
                Instructions:
                1. Analyze the processes and network connections for malicious indicators.
                2. Correlate with the provided MITRE context if applicable.
                3. Provide a risk assessment summary.
                
                Format your response as a concise security alert report.
                """;

        PromptTemplate template = new PromptTemplate(promptText);

        Map<String, Object> params = Map.of(
                "rag_context", ragContext,
                "cpu", report.getCpuUsage(),
                "ram", report.getRamUsedPercent(),
                "processes", report.getProcesses().toString(),
                "network", report.getNetworkConnections().toString()
        );

        Prompt prompt = template.create(params);

        return chatClient.call(prompt).getResult().getOutput().getContent();
    }
}