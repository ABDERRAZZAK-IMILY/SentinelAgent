package com.sentinelagent.backend.telemetry;

import lombok.Value;

import java.util.UUID;


@Value
public class MetricReportId {
    String value;

    public static MetricReportId generate() {
        return new MetricReportId(UUID.randomUUID().toString());
    }

    public static MetricReportId of(String id) {
        return new MetricReportId(id);
    }
}
