package com.sentinelagent.backend.report.internal.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "reports")
public class ReportDocument {

    @Id
    private String id;

    @Indexed
    private String agentId;

    private String reportType;
    private String contentUrl;
    private String aiSummary;

    private LocalDateTime generatedAt;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}
