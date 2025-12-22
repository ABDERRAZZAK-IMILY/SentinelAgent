package com.sentinelagent.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkConnectionRequest {
    private int pid;

    @JsonProperty("local_address")
    private String localAddress;

    @JsonProperty("local_port")
    private int localPort;

    @JsonProperty("remote_address")
    private String remoteAddress;

    @JsonProperty("remote_port")
    private int remotePort;

    @JsonProperty("process_name")
    private String processName;


    private String status;
}