package com.sentinelagent.backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MetricReportResponse {
    private String reportId;
    private double cpuUsage;
    private double ramUsage;
    private double diskUsage;
    private int processCount;
    private int networkConnectionsCount;
    private LocalDateTime timestamp;

}