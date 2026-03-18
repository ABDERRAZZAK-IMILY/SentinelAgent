package com.sentinelagent.backend.telemetry.internal.service;

import com.sentinelagent.backend.agent.api.AgentValidationService;
import com.sentinelagent.backend.agent.api.InvalidAgentCredentialsException;
import com.sentinelagent.backend.telemetry.dto.TelemetryAiSummaryResponse;
import com.sentinelagent.backend.telemetry.dto.TelemetryData;
import com.sentinelagent.backend.telemetry.dto.TelemetryResponse;
import com.sentinelagent.backend.telemetry.event.TelemetryReceivedEvent;
import com.sentinelagent.backend.telemetry.internal.domain.MetricReport;
import com.sentinelagent.backend.telemetry.internal.domain.MetricReportDocument;
import com.sentinelagent.backend.telemetry.internal.mapper.MetricReportMapper;
import com.sentinelagent.backend.telemetry.internal.mapper.TelemetryMapper;
import com.sentinelagent.backend.telemetry.internal.repository.SpringDataMetricReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final AgentValidationService agentValidationService;
    private final SpringDataMetricReportRepository metricReportRepository;
    private final MetricReportMapper metricReportMapper;
    private final TelemetryMapper telemetryMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public MetricReport processAndSaveTelemetry(TelemetryData data) {
        AgentValidationService.AgentValidationResult validationResult = agentValidationService
                .validate(data.getAgentId(), data.getApiKey());
        if (validationResult == null) {
            throw new InvalidAgentCredentialsException("Unknown agent");
        }

        MetricReport report = telemetryMapper.toMetricReport(data);
        MetricReportDocument saved = metricReportRepository.save(metricReportMapper.toDocument(report));
        MetricReport savedReport = metricReportMapper.toDomain(saved);

        TelemetryReceivedEvent event = telemetryMapper.toEvent(savedReport);
        eventPublisher.publishEvent(event);
        return savedReport;
    }

    @Override
    public List<TelemetryResponse> getHistory(String agentId, LocalDateTime from, LocalDateTime to) {
        return metricReportRepository
                .findByAgentIdAndReceivedAtBetweenOrderByReceivedAtAsc(agentId, from, to)
                .stream()
                .map(metricReportMapper::toDomain)
                .map(telemetryMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<TelemetryResponse> getLatest(String agentId) {
        return metricReportRepository.findTopByAgentIdOrderByReceivedAtDesc(agentId)
                .map(metricReportMapper::toDomain)
                .map(telemetryMapper::toResponse);
    }

    @Override
    public TelemetryAiSummaryResponse getAiSummary(String agentId, LocalDateTime from, LocalDateTime to) {
        List<MetricReport> reports = metricReportRepository
                .findByAgentIdAndReceivedAtBetweenOrderByReceivedAtAsc(agentId, from, to)
                .stream()
                .map(metricReportMapper::toDomain)
                .toList();
        return telemetryMapper.toAiSummary(agentId, reports, from, to);
    }
}

