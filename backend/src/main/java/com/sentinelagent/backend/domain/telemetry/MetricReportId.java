package com.sentinelagent.backend.domain.telemetry;

import lombok.Value;

import java.util.UUID;

/**
 * Value Object for MetricReport identifier.
 * Part of the Domain Layer.
 */
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
