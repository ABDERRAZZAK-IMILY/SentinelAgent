package com.sentinelagent.backend.api.v1.agent;

import com.sentinelagent.backend.application.agent.*;
import com.sentinelagent.backend.application.agent.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Agent Management.
 * Handles agent registration, heartbeat, and queries.
 */
@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final RegisterAgentUseCase registerAgentUseCase;
    private final ProcessHeartbeatUseCase processHeartbeatUseCase;
    private final GetAgentsUseCase getAgentsUseCase;

    /**
     * Register a new agent
     * POST /api/v1/agents/register
     */
    @PostMapping("/register")
    public ResponseEntity<AgentRegistrationResponse> registerAgent(
            @Valid @RequestBody AgentRegistrationRequest request) {
        AgentRegistrationResponse response = registerAgentUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Process agent heartbeat
     * POST /api/v1/agents/heartbeat
     */
    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @RequestHeader("X-Agent-Key") String apiKey,
            @Valid @RequestBody HeartbeatRequest request) {
        processHeartbeatUseCase.execute(apiKey, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all registered agents
     * GET /api/v1/agents
     */
    @GetMapping
    public ResponseEntity<List<AgentDetailsDto>> getAllAgents() {
        return ResponseEntity.ok(getAgentsUseCase.getAllAgents());
    }

    /**
     * Get agent by ID
     * GET /api/v1/agents/{agentId}
     */
    @GetMapping("/{agentId}")
    public ResponseEntity<AgentDetailsDto> getAgentById(@PathVariable String agentId) {
        return ResponseEntity.ok(getAgentsUseCase.getById(agentId));
    }

    /**
     * Get agents by status
     * GET /api/v1/agents/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgentDetailsDto>> getAgentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(getAgentsUseCase.getAgentsByStatus(status));
    }

    /**
     * Get agent statistics
     * GET /api/v1/agents/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<GetAgentsUseCase.AgentStatsDto> getStats() {
        return ResponseEntity.ok(getAgentsUseCase.getStats());
    }
}
