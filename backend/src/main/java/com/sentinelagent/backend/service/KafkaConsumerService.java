package com.sentinelagent.backend.service;

import com.sentinelagent.backend.dto.request.MetricReportRequest;
import com.sentinelagent.backend.mapper.MetricReportMapper;
import com.sentinelagent.backend.model.MetricReport;
import com.sentinelagent.backend.repository.MetricReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConsumerService {


    private final MetricReportRepository repository;
    private final MetricReportMapper mapper;

    public KafkaConsumerService(MetricReportRepository repository, MetricReportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "agent-data", groupId = "sentinel-consumer-group")
    public void consume(MetricReportRequest request) {
        log.info(" Received MetricReportRequest from Kafka");

        MetricReport entity = mapper.toEntity(request);

        MetricReport saved = repository.save(entity);

        log.info(" Saved report to DB with ID: {}", saved.getId());
    }
}