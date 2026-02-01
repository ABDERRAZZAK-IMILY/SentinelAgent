package com.sentinelagent.backend.infrastructure.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MITRE ATT&CK Knowledge Loader.
 * Loads security data into the vector store on startup.
 * Part of the Infrastructure Layer.
 */
@Slf4j
@Component
public class MitreKnowledgeLoader implements CommandLineRunner {

    private final VectorStore vectorStore;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${security.mitre.url}")
    private String mitreUrl;

    public MitreKnowledgeLoader(VectorStore vectorStore, RestClient.Builder builder, ObjectMapper objectMapper) {
        this.vectorStore = vectorStore;
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        log.info("🚀 Starting MITRE ATT&CK Knowledge Ingestion from remote source...");

        try {
            String rawJson = restClient.get()
                    .uri(mitreUrl)
                    .retrieve()
                    .body(String.class);

            if (rawJson == null)
                return;

            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode objects = rootNode.get("objects");

            List<Document> documents = new ArrayList<>();

            if (objects.isArray()) {
                for (JsonNode obj : objects) {
                    if (obj.has("type") && "attack-pattern".equals(obj.get("type").asText())) {

                        if (obj.has("x_mitre_deprecated") && obj.get("x_mitre_deprecated").asBoolean()) {
                            continue;
                        }

                        String name = obj.has("name") ? obj.get("name").asText() : "Unknown";
                        String description = obj.has("description") ? obj.get("description").asText() : "";

                        String mitreId = "Unknown";
                        if (obj.has("external_references")) {
                            for (JsonNode ref : obj.get("external_references")) {
                                if (ref.has("source_name") && "mitre-attack".equals(ref.get("source_name").asText())) {
                                    mitreId = ref.get("external_id").asText();
                                    break;
                                }
                            }
                        }

                        String content = "Technique: " + name + " (" + mitreId + "). Description: " + description;

                        Map<String, Object> metadata = Map.of(
                                "source", "MITRE ATT&CK",
                                "mitre_id", mitreId,
                                "technique_name", name);

                        documents.add(new Document(content, metadata));
                    }
                }
            }

            if (!documents.isEmpty()) {
                log.info("📚 Found {} techniques. Saving to Vector DB...", documents.size());
                vectorStore.add(documents);
                log.info("✅ MITRE Knowledge Base is ready! System is now smarter.");
            }

        } catch (Exception e) {
            log.error("❌ Failed to load MITRE data: {}", e.getMessage());
        }
    }
}
