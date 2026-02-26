package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.telemetry.TelemetryReceivedEvent;
import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.NetworkConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AnalyzeSecurityUseCase {

    private final ChatModel chatModel;
    private final RagSecurityUseCase ragSecurityUseCase;
    private final NetworkIntelligenceUseCase networkIntelligence;

    public String execute(MetricReport report) {
        String networkContext = enrichNetworkData(report.getNetworkConnections());

        double uploadMB = report.getUploadSpeedMbps();
        double downloadMB = report.getDownloadSpeedMbps();

        String ragContext = ragSecurityUseCase.findMitigationStrategy(
                "High resource usage or suspicious network connection");
        if (ragContext == null)
            ragContext = "No specific MITRE data found.";

        String promptText = """
                You are an advanced Cybersecurity AI Agent powered by DeepSeek.
                Your task is to analyze system metrics and detect potential threats (Ransomware, Spyware, C2 Communication).

                --- INTELLIGENCE CONTEXT ---
                Knowledge Base (MITRE ATT&CK):
                {rag_context}

                Network Intelligence (GeoIP & Reputation):
                {network_context}

                --- LIVE SYSTEM METRICS ---
                - CPU Usage: {cpu}%
                - RAM Usage: {ram}%
                - Network Upload Speed: {upload} MB/s
                - Network Download Speed: {download} MB/s
                - Active Processes: {processes}

                --- INSTRUCTIONS ---
                1. Analyze 'Network Intelligence'. If a known malicious IP is found, FLAGGED immediately.
                2. Check if 'Network Upload Speed' is high while CPU is high (Potential Data Theft).
                3. Look at the process names in the network connections. Is a weird process connecting to the internet?
                4. Output a concise JSON alert containing the following keys: risk_level, threat_type, description, recommendation.
                """;

        PromptTemplate template = new PromptTemplate(promptText);

        Map<String, Object> params = Map.of(
                "rag_context", ragContext,
                "network_context", networkContext,
                "cpu", report.getCpuUsage(),
                "ram", report.getRamUsedPercent(),
                "upload", String.format("%.2f", uploadMB),
                "download", String.format("%.2f", downloadMB),
                "processes", report.getProcesses() != null ? report.getProcesses().toString() : "No processes");

        Prompt prompt = template.create(params);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    /**
     * Execute security analysis from a TelemetryReceivedEvent.
     * Reconstructs the minimal MetricReport needed for AI analysis.
     */
    public String executeFromEvent(TelemetryReceivedEvent event) {
        MetricReport report = MetricReport.builder()
                .agentId(event.agentId())
                .hostname(event.hostname())
                .cpuUsage(event.cpuUsage())
                .ramUsedPercent(event.ramUsedPercent())
                .bytesSentSec(event.bytesSentSec())
                .bytesRecvSec(event.bytesRecvSec())
                .processes(event.processes() != null ? event.processes().stream()
                        .map(p -> com.sentinelagent.backend.telemetry.Process.builder()
                                .pid(p.pid())
                                .name(p.name())
                                .cpuUsage(p.cpuUsage())
                                .username(p.username())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .networkConnections(event.networkConnections() != null ? event.networkConnections().stream()
                        .map(n -> NetworkConnection.builder()
                                .pid(n.pid())
                                .processName(n.processName())
                                .remoteAddress(n.remoteAddress())
                                .remotePort(n.remotePort())
                                .status(n.status())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();

        return execute(report);
    }

    private String enrichNetworkData(List<NetworkConnection> connections) {
        if (connections == null || connections.isEmpty()) {
            return "No active network connections.";
        }

        return connections.stream()
                .map(conn -> {
                    String ip = conn.getRemoteAddress();
                    String country = networkIntelligence.getCountryByIp(ip);
                    boolean isMalicious = networkIntelligence.isMaliciousIp(ip);

                    String pName = (conn.getProcessName() != null) ? conn.getProcessName() : "Unknown";

                    return String.format(
                            "- Process: %s | Remote IP: %s | Location: %s | Reputation: %s",
                            pName,
                            ip,
                            country,
                            isMalicious ? "MALICIOUS ⚠️" : "Safe");
                })
                .collect(Collectors.joining("\n"));
    }
}
