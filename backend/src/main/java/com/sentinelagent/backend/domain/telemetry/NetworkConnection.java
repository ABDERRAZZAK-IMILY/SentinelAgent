package com.sentinelagent.backend.domain.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain Entity representing a network connection.
 * Part of the Domain Layer - no external dependencies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NetworkConnection {
    private int pid;
    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private String status;
    private String processName;
}
