package com.sentinelagent.backend.securityanalysis.internal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentinelagent.backend.securityanalysis.internal.domain.AnalysisResult;
import com.sentinelagent.backend.telemetry.event.TelemetryReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SecurityAnalysisServiceImpl implements SecurityAnalysisService {

    private static final double SIMILARITY_THRESHOLD = 0.70;

    private final ChatModel chatModel;
    private final VectorStore vectorStore;
    private final RestClient restClient;

    @Value("${security.api.abuseipdb.key}")
    private String apiKey;

    @Value("${security.api.abuseipdb.url}")
    private String apiUrl;

    public SecurityAnalysisServiceImpl(ChatModel chatModel, VectorStore vectorStore, RestClient.Builder builder) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
        this.restClient = builder.build();
    }

    @Override
    public AnalysisResult analyzeTelemetry(TelemetryReceivedEvent event) {
        double uploadMB = event.bytesSentSec() / 1024.0 / 1024.0;
        double downloadMB = event.bytesRecvSec() / 1024.0 / 1024.0;

        String networkContext = enrichNetworkData(event.networkConnections());
        String ragContext = findMitigationStrategy("High resource usage or suspicious network connection");
        if (ragContext == null) {
            ragContext = "No specific MITRE data found.";
        }

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
                "hostname", event.hostname() != null ? event.hostname() : "Unknown-Host",
                "cpu", event.cpuUsage(),
                "ram", event.ramUsedPercent(),
                "format", outputConverter.getFormat(),
                "upload", String.format("%.2f", uploadMB),
                "download", String.format("%.2f", downloadMB),
                "processes", event.processes() != null ? event.processes().toString() : "No processes");

        try {
            String response = chatModel.call(template.create(params)).getResult().getOutput().getText();
            log.debug("[SecurityAnalysisService] Raw AI response: {}", response);
            String cleaned = cleanJsonResponse(response);
            log.debug("[SecurityAnalysisService] Cleaned AI response: {}", cleaned);
            return outputConverter.convert(cleaned);
        } catch (Exception ex) {
            log.error("[SecurityAnalysisService] Failed to convert AI response", ex);
            throw ex;
        }
    }

    private String cleanJsonResponse(String response) {
        if (response == null) {
            return "{}";
        }
        return response.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String findMitigationStrategy(String threatDescription) {
        SearchRequest request = SearchRequest.builder()
                .query(threatDescription)
                .topK(2)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        List<Document> similarDocs = vectorStore.similaritySearch(request);

        if (similarDocs.isEmpty()) {
            log.warn("⚠️ No relevant knowledge found in vector store for this threat.");
            return "No specific playbook found in the knowledge base. Recommended action: Manual investigation and host isolation.";
        }

        return similarDocs.stream()
                .map(this::formatDocumentResponse)
                .collect(Collectors.joining("\n---\n"));
    }

    private String formatDocumentResponse(Document doc) {
        String technique = (String) doc.getMetadata().getOrDefault("technique_name", "Unknown Technique");
        String mitreId = (String) doc.getMetadata().getOrDefault("mitre_id", "T????");
        String content = doc.getText();
        return String.format("""
                 **MITRE ATT&CK Match:** %s (%s)
                 **Insight:** %s
                """, technique, mitreId, content);
    }

    private String enrichNetworkData(List<TelemetryReceivedEvent.NetworkConnectionInfo> connections) {
        if (connections == null || connections.isEmpty()) {
            return "No active network connections.";
        }

        return connections.stream()
                .map(conn -> {
                    String ip = conn.remoteAddress();
                    String country = getCountryByIp(ip);
                    boolean isMalicious = isMaliciousIp(ip);
                    String pName = (conn.processName() != null) ? conn.processName() : "Unknown";
                    return String.format(
                            "- Process: %s | Remote IP: %s | Location: %s | Reputation: %s",
                            pName,
                            ip,
                            country,
                            isMalicious ? "MALICIOUS ⚠️" : "Safe");
                })
                .collect(Collectors.joining("\n"));
    }

    private boolean isMaliciousIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        if (ip.startsWith("192.168") || ip.startsWith("127.") || ip.startsWith("10.")) {
            return false;
        }

        try {
            JsonNode response = restClient.get()
                    .uri(apiUrl + "?ipAddress=" + ip)
                    .header("Key", apiKey)
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("data")) {
                int score = response.get("data").get("abuseConfidenceScore").asInt();
                return score > 50;
            }
        } catch (Exception e) {
            log.error("🚫 API Call Failed for IP: {} - {}", ip, e.getMessage());
        }

        return false;
    }

    private String getCountryByIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "Unknown";
        }
        try {
            JsonNode response = restClient.get()
                    .uri("http://ip-api.com/json/" + ip)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("country")) {
                return response.get("country").asText();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not get country for IP: {}", ip);
            return "Unknown";
        }
        return "Unknown";
    }
}
