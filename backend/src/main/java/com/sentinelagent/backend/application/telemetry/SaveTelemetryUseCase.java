package com.sentinelagent.backend.application.telemetry;

import com.sentinelagent.backend.application.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.domain.telemetry.MetricReport;
import com.sentinelagent.backend.domain.telemetry.MetricReportRepository;
import com.sentinelagent.backend.domain.telemetry.NetworkConnection;
import com.sentinelagent.backend.domain.telemetry.Process;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use Case for saving telemetry data.
 * Part of the Application Layer.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SaveTelemetryUseCase {

    private final MetricReportRepository metricReportRepository;

    public MetricReport execute(TelemetryData data) {
        log.info("💾 Saving telemetry from agent: {}", data.getAgentId());

        // Convert to domain entity
        MetricReport report = MetricReport.builder()
                .agentId(data.getAgentId())
                .hostname(data.getHostname())
                .cpuUsage(data.getCpuUsage())
                .ramUsedPercent(data.getRamUsedPercent())
                .ramTotalMb(data.getRamTotalMb())
                .diskUsedPercent(data.getDiskUsedPercent())
                .diskTotalGb(data.getDiskTotalGb())
                .bytesSentSec(data.getBytesSentSec())
                .bytesRecvSec(data.getBytesRecvSec())
                .processes(mapProcesses(data.getProcesses()))
                .networkConnections(mapConnections(data.getNetworkConnections()))
                .receivedAt(LocalDateTime.now())
                .build();

        return metricReportRepository.save(report);
    }

    private List<Process> mapProcesses(List<TelemetryData.ProcessData> processes) {
        if (processes == null)
            return List.of();
        return processes.stream()
                .map(p -> Process.builder()
                        .pid(p.getPid())
                        .name(p.getName())
                        .cpuUsage(p.getCpu())
                        .username(p.getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    private List<NetworkConnection> mapConnections(List<TelemetryData.NetworkConnectionData> connections) {
        if (connections == null)
            return List.of();
        return connections.stream()
                .map(c -> NetworkConnection.builder()
                        .pid(c.getPid())
                        .localAddress(c.getLocalAddress())
                        .localPort(c.getLocalPort())
                        .remoteAddress(c.getRemoteAddress())
                        .remotePort(c.getRemotePort())
                        .status(c.getStatus())
                        .processName(c.getProcessName())
                        .build())
                .collect(Collectors.toList());
    }
}
