package com.sentinelagent.backend.domain.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Entity representing a system process.
 * Part of the Domain Layer - no external dependencies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Process {
    private int pid;
    private String name;
    private double cpuUsage;
    private String username;
}
