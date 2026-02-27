package com.sentinelagent.backend.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
