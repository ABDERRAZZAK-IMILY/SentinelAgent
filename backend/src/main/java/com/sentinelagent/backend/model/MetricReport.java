package com.sentinelagent.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "agent_reports")
public class MetricReport {

    @Id
    private String id;

    private double cpuUsage;
    private double ramUsedPercent;
    private long ramTotalMb;
    private double diskUsedPercent;
    private long diskTotalGb;

    private List<ProcessModel> processes;
    private List<NetworkConnectionModel> networkConnections;

    private LocalDateTime receivedAt;
}