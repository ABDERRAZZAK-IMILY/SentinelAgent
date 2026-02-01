package com.sentinelagent.backend.application.security;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Use Case for network intelligence operations (IP reputation, GeoIP).
 * Part of the Application Layer.
 */
@Service
@Slf4j
public class NetworkIntelligenceUseCase {

    private final RestClient restClient;

    @Value("${security.api.abuseipdb.key}")
    private String apiKey;

    @Value("${security.api.abuseipdb.url}")
    private String apiUrl;

    public NetworkIntelligenceUseCase(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public boolean isMaliciousIp(String ip) {
        // Skip private IPs
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

    public String getCountryByIp(String ip) {
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
