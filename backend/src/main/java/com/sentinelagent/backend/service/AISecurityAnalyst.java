package com.sentinelagent.backend.service;

import com.sentinelagent.backend.model.MetricReport;
import com.sentinelagent.backend.model.NetworkConnectionModel;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AISecurityAnalyst {

    private final ChatClient chatClient;
    private final RagSecurityService ragService;
    private final NetworkIntelligenceService networkIntel;

    public AISecurityAnalyst(ChatClient chatClient,
                             RagSecurityService ragService,
                             NetworkIntelligenceService networkIntel) {
        this.chatClient = chatClient;
        this.ragService = ragService;
        this.networkIntel = networkIntel;
    }

    public String analyzeRisk(MetricReport report) {

        String networkContext = enrichNetworkData(report.getNetworkConnections());

        String ragContext = ragService.findMitigationStrategy("High resource usage or suspicious network connection");

        String promptText = """
                You are an advanced Cybersecurity AI Agent powered by DeepSeek.
                Your task is to analyze system metrics and detect potential threats.
                
                --- INTELLIGENCE CONTEXT ---
                Knowledge Base (MITRE ATT&CK):
                {rag_context}
                
                Network Intelligence (GeoIP & Reputation):
                {network_context}
                
                --- LIVE SYSTEM METRICS ---
                - CPU Usage: {cpu}%
                - RAM Usage: {ram}%
                - Active Processes: {processes}
                
                --- INSTRUCTIONS ---
                1. Combine the live metrics with the provided Network Intelligence.
                2. If a malicious IP is detected in 'Network Intelligence', prioritize it as a threat.
                3. Use the MITRE context to suggest specific mitigation steps.
                4. Output a concise JSON alert with fields: {risk_level, threat_type, description, recommendation}.
                """;

        PromptTemplate template = new PromptTemplate(promptText);

        Map<String, Object> params = Map.of(
                "rag_context", ragContext,
                "network_context", networkContext,
                "cpu", report.getCpuUsage(),
                "ram", report.getRamUsedPercent(),
                "processes", report.getProcesses().toString()
        );

        Prompt prompt = template.create(params);

        return chatClient.call(prompt).getResult().getOutput().getContent();
    }

    private String enrichNetworkData(List<NetworkConnectionModel> connections) {
        if (connections == null || connections.isEmpty()) return "No active network connections.";

        return connections.stream()
                .map(conn -> {
                    String ip = conn.getRemoteAddress();
                    String country = networkIntel.getCountryByIp(ip);
                    boolean isMalicious = networkIntel.isMaliciousIp(ip);

                    return String.format(
                            "- Remote IP: %s | Port: %d | Location: %s | Reputation: %s",
                            ip,
                            conn.getRemotePort(),
                            country,
                            isMalicious ? "MALICIOUS " : "Safe"
                    );
                })
                .collect(Collectors.joining("\n"));
    }
}