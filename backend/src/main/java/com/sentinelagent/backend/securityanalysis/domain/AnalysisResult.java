package com.sentinelagent.backend.securityanalysis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnalysisResult(
        @JsonProperty("risk_level") String riskLevel,
        @JsonProperty("threat_type") String threatType,
        @JsonProperty("description") String description,
        @JsonProperty("recommendation") String recommendation
) {}