package com.sentinelagent.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(RagSecurityService.class);
    private final VectorStore vectorStore;

    // threshold is 70% similarity
    private static final double SIMILARITY_THRESHOLD = 0.70;

    public RagSecurityService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }


    // the method to perform RAG search and return mitigation strategies
    public String findMitigationStrategy(String threatDescription) {
        logger.info(" Performing RAG search for: [{}]", threatDescription);

        SearchRequest request = SearchRequest.query(threatDescription)
                .withTopK(2)
                .withSimilarityThreshold(SIMILARITY_THRESHOLD);

        List<Document> similarDocs = vectorStore.similaritySearch(request);

        if (similarDocs.isEmpty()) {
            logger.warn(" No relevant knowledge found in Qdrant for this threat.");
            return "No specific playbook found in the knowledge base. Recommended action: Manual investigation and host isolation.";
        }

        return similarDocs.stream()
                .map(this::formatDocumentResponse)
                .collect(Collectors.joining("\n---\n"));
    }

    private String formatDocumentResponse(Document doc) {
        String technique = (String) doc.getMetadata().getOrDefault("technique_name", "Unknown Technique");
        String mitreId = (String) doc.getMetadata().getOrDefault("mitre_id", "T????");
        String content = doc.getContent();

        return String.format("""
                 **MITRE ATT&CK Match:** %s (%s)
                 **Insight:** %s
                """, technique, mitreId, content);
    }
}