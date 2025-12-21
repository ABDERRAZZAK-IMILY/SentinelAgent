package com.sentinelagent.backend.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessModel {
    private int pid;
    private String name;
    private double cpuUsage;
    private String username;
}