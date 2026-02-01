package com.sentinelagent.backend.infrastructure.persistence.repository;

import com.sentinelagent.backend.domain.telemetry.MetricReport;
import com.sentinelagent.backend.domain.telemetry.MetricReportId;
import com.sentinelagent.backend.domain.telemetry.MetricReportRepository;
import com.sentinelagent.backend.infrastructure.persistence.entity.MetricReportDocument;
import com.sentinelagent.backend.infrastructure.persistence.mapper.MetricReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB implementation of the MetricReportRepository port.
 * Bridges the domain layer with Spring Data MongoDB.
 */
@Repository
@RequiredArgsConstructor
public class MongoMetricReportRepository implements MetricReportRepository {

    private final SpringDataMetricReportRepository springDataRepository;
    private final MetricReportMapper mapper;

    @Override
    public MetricReport save(MetricReport report) {
        MetricReportDocument document = mapper.toDocument(report);
        MetricReportDocument saved = springDataRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<MetricReport> findById(MetricReportId id) {
        return springDataRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<MetricReport> findByAgentId(String agentId) {
        return springDataRepository.findByAgentId(agentId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetricReport> findByHostname(String hostname) {
        return springDataRepository.findByHostname(hostname).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetricReport> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end) {
        return springDataRepository.findByReceivedAtBetween(start, end).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetricReport> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(MetricReportId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
