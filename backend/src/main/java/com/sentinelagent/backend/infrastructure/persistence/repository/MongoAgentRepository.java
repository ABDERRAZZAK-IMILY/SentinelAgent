package com.sentinelagent.backend.infrastructure.persistence.repository;

import com.sentinelagent.backend.domain.agent.*;
import com.sentinelagent.backend.infrastructure.persistence.entity.AgentDocument;
import com.sentinelagent.backend.infrastructure.persistence.mapper.AgentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB implementation of the AgentRepository port.
 * Bridges the domain layer with Spring Data MongoDB.
 */
@Repository
@RequiredArgsConstructor
public class MongoAgentRepository implements AgentRepository {

    private final SpringDataAgentRepository springDataRepository;
    private final AgentMapper mapper;

    @Override
    public Agent save(Agent agent) {
        AgentDocument document = mapper.toDocument(agent);
        AgentDocument saved = springDataRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Agent> findById(AgentId id) {
        return springDataRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Agent> findByApiKeyHash(String apiKeyHash) {
        return springDataRepository.findByApiKeyHash(apiKeyHash)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Agent> findByHostname(String hostname) {
        return springDataRepository.findByHostname(hostname)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByHostname(String hostname) {
        return springDataRepository.existsByHostname(hostname);
    }

    @Override
    public List<Agent> findByStatus(AgentStatus status) {
        return springDataRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Agent> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(AgentId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public long countByStatus(AgentStatus status) {
        return springDataRepository.countByStatus(status.name());
    }
}
