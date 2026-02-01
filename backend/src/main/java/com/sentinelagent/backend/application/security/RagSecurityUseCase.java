package com.sentinelagent.backend.application.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case for RAG-based security knowledge retrieval.
 * Part of the Application Layer.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RagSecurityUseCase {

    private final VectorStore vectorStore;
    private static final double SIMILARITY_THRESHOLD = 0.70;

    public String findMitigationStrategy(String threatDescription) {
        log.info("🔍 Performing RAG search for: [{}]", threatDescription);

        SearchRequest request = SearchRequest.builder()
                .query(threatDescription)
                .topK(2)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        List<Document> similarDocs = vectorStore.similaritySearch(request);

        if (similarDocs.isEmpty()) {
            log.warn("⚠️ No relevant knowledge found in Qdrant for this threat.");
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
}
