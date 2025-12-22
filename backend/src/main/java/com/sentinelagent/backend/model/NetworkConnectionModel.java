package com.sentinelagent.backend.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkConnectionModel {
    private int pid;
    private String localAddress;
    private int localPort;
    private String remoteAddress;
    private int remotePort;
    private String status;

    private String processName;
}