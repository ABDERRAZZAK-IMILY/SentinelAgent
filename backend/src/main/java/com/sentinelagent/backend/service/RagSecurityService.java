package com.sentinelagent.backend.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RagSecurityService {

    private final VectorStore vectorStore;

    public RagSecurityService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestKnowledgeBase() {
        List<Document> documents = List.of(
                new Document("Cryptojacking involves high CPU usage by unknown processes like 'xmrig'. Mitigation: Kill process and block mining pools IPs.", Map.of("threat_type", "mining")),
                new Document("Reverse Shell connection often uses tools like 'nc.exe' or 'powershell'. Mitigation: Isolate host and scan for backdoors.", Map.of("threat_type", "intrusion"))
        );

        vectorStore.add(documents);
        System.out.println(" Knowledge Base Ingested into Qdrant!");
    }

    public String searchForMitigation(String threatDescription) {
        List<Document> similarDocs = vectorStore.similaritySearch(
                SearchRequest.query(threatDescription).withTopK(1)
        );

        if (similarDocs.isEmpty()) {
            return "No specific mitigation found in knowledge base.";
        }

        return similarDocs.get(0).getContent();
    }
}