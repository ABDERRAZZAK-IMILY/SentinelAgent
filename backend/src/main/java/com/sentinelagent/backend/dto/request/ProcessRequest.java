package com.sentinelagent.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessRequest {
    private int pid;
    private String name;
    private double cpu;
    private String username;
}