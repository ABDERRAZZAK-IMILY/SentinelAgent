package com.sentinelagent.backend.mapper;

import com.sentinelagent.backend.dto.request.MetricReportRequest;
import com.sentinelagent.backend.dto.request.NetworkConnectionRequest;
import com.sentinelagent.backend.dto.request.ProcessRequest;
import com.sentinelagent.backend.model.MetricReport;
import com.sentinelagent.backend.model.NetworkConnectionModel;
import com.sentinelagent.backend.model.ProcessModel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MetricReportMapper {

    public MetricReport toEntity(MetricReportRequest req) {
        if (req == null) {
            return null;
        }

        MetricReport report = new MetricReport();
        report.setCpuUsage(req.getCpu());
        report.setRamUsedPercent(req.getRamUsedPercent());
        report.setRamTotalMb(req.getRamTotalMb());
        report.setDiskUsedPercent(req.getDiskUsedPercent());
        report.setDiskTotalGb(req.getDiskTotalGb());

        report.setReceivedAt(LocalDateTime.now());

        report.setProcesses(mapProcesses(req.getProcesses()));
        report.setNetworkConnections(mapNetworkConnections(req.getNetworkConnections()));

        return report;
    }

    private List<ProcessModel> mapProcesses(List<ProcessRequest> processRequests) {
        if (processRequests == null) {
            return Collections.emptyList();
        }
        return processRequests.stream()
                .map(this::toProcessModel)
                .collect(Collectors.toList());
    }

    private ProcessModel toProcessModel(ProcessRequest req) {
        return new ProcessModel(
                req.getPid(),
                req.getName(),
                req.getCpu(),
                req.getUsername()
        );
    }

    private List<NetworkConnectionModel> mapNetworkConnections(List<NetworkConnectionRequest> networkRequests) {
        if (networkRequests == null) {
            return Collections.emptyList();
        }
        return networkRequests.stream()
                .map(this::toNetworkModel)
                .collect(Collectors.toList());
    }

    private NetworkConnectionModel toNetworkModel(NetworkConnectionRequest req) {
        return new NetworkConnectionModel(
                req.getPid(),
                req.getLocalAddress(),
                req.getLocalPort(),
                req.getRemoteAddress(),
                req.getRemotePort(),
                req.getStatus()
        );
    }
}