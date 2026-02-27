package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.securityanalysis.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.TelemetryReceivedEvent;
import com.sentinelagent.backend.telemetry.MetricReport;
import com.sentinelagent.backend.telemetry.NetworkConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
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

    public AnalysisResult execute(MetricReport report) {
        String networkContext = enrichNetworkData(report.getNetworkConnections());

        double uploadMB = report.getUploadSpeedMbps();
        double downloadMB = report.getDownloadSpeedMbps();

        String ragContext = ragSecurityUseCase.findMitigationStrategy(
                "High resource usage or suspicious network connection");
        if (ragContext == null)
            ragContext = "No specific MITRE data found.";


        var outputConverter = new BeanOutputConverter<>(AnalysisResult.class);


        String promptText = """
               You are an advanced Cybersecurity AI Agent specialized in real-time threat detection.
               Your mission is to analyze system telemetry and intelligence context to identify potential security breaches such as Ransomware, Spyware, or C2 Communication.
    
               --- INTELLIGENCE CONTEXT (Threat Intel & RAG) ---
               Knowledge Base (MITRE ATT&CK): 
               {rag_context}
    
               Network Intelligence (GeoIP & Reputation): 
               {network_context}
    
               --- LIVE SYSTEM TELEMETRY ---
               - Hostname: {hostname}
               - CPU Usage: {cpu}%
               - RAM Usage: {ram}%
               - Network Upload Speed: {upload} MB/s
               - Network Download Speed: {download} MB/s
               - Active Processes: {processes}
    
    --- ANALYSIS INSTRUCTIONS ---
    1. REPUTATION CHECK: Scan the 'Network Intelligence' for any malicious IPs. If found, elevate risk immediately.
    2. ANOMALY DETECTION: High upload speeds (Exfiltration) combined with high CPU (Encryption/Hashing) are primary indicators of Ransomware or Data Theft.
    3. PROCESS SCRUTINY: Check process names in network connections. Flag unauthorized binaries communicating with external IPs.
    4. RISK DETERMINATION: Classify the risk as SAFE, LOW, MEDIUM, HIGH, or CRITICAL.
    
    --- OUTPUT REQUIREMENTS ---
    {format}
    
    STRICT RULE: Return ONLY the raw JSON object. Do NOT wrap it in markdown code blocks (like ```json) and do not provide any text explanation outside the JSON.
    """;

        PromptTemplate template = new PromptTemplate(promptText);

        Map<String, Object> params = Map.of(
                "rag_context", ragContext,
                "network_context", networkContext,
                "hostname", report.getHostname() != null ? report.getHostname() : "Unknown-Host",
                "cpu", report.getCpuUsage(),
                "ram", report.getRamUsedPercent(),
                "format" , outputConverter.getFormat(),
                "upload", String.format("%.2f", uploadMB),
                "download", String.format("%.2f", downloadMB),
                "processes", report.getProcesses() != null ? report.getProcesses().toString() : "No processes");

        String response = chatModel.call(template.create(params)).getResult().getOutput().getText();
        return outputConverter.convert(response);

//        Prompt prompt = template.create(params);
//        return chatModel.call(prompt).getResult().getOutput().getText();
    }



    public AnalysisResult executeFromEvent(TelemetryReceivedEvent event) {
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
                        .collect(java.util.stream.Collectors.toList()) : java.util.List.of())
                .build();

        return execute(report);
    }
//    public AnalysisResult executeFromEvent(TelemetryReceivedEvent event) {
//        MetricReport report = MetricReport.builder()
//                .agentId(event.agentId())
//                .hostname(event.hostname())
//                .cpuUsage(event.cpuUsage())
//                .ramUsedPercent(event.ramUsedPercent())
//                .bytesSentSec(event.bytesSentSec())
//                .bytesRecvSec(event.bytesRecvSec())
//                .processes(event.processes() != null ? event.processes().stream()
//                        .map(p -> com.sentinelagent.backend.telemetry.Process.builder()
//                                .pid(p.pid())
//                                .name(p.name())
//                                .cpuUsage(p.cpuUsage())
//                                .username(p.username())
//                                .build())
//                        .collect(Collectors.toList()) : List.of())
//                .networkConnections(event.networkConnections() != null ? event.networkConnections().stream()
//                        .map(n -> NetworkConnection.builder()
//                                .pid(n.pid())
//                                .processName(n.processName())
//                                .remoteAddress(n.remoteAddress())
//                                .remotePort(n.remotePort())
//                                .status(n.status())
//                                .build())
//                        .collect(Collectors.toList()) : List.of())
//                .build();
//
//        return execute(report);
//    }

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
