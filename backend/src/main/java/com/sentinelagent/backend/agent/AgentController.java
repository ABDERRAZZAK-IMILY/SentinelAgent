package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.agent.*;
import com.sentinelagent.backend.agent.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final RegisterAgentUseCase registerAgentUseCase;
    private final ProcessHeartbeatUseCase processHeartbeatUseCase;
    private final GetAgentsUseCase getAgentsUseCase;


    @PostMapping("/register")
    public ResponseEntity<AgentRegistrationResponse> registerAgent(
            @Valid @RequestBody AgentRegistrationRequest request) {
        AgentRegistrationResponse response = registerAgentUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @RequestHeader("X-Agent-Key") String apiKey,
            @Valid @RequestBody HeartbeatRequest request) {
        processHeartbeatUseCase.execute(apiKey, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AgentDetailsDto>> getAllAgents() {
        return ResponseEntity.ok(getAgentsUseCase.getAllAgents());
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentDetailsDto> getAgentById(@PathVariable String agentId) {
        return ResponseEntity.ok(getAgentsUseCase.getById(agentId));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgentDetailsDto>> getAgentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(getAgentsUseCase.getAgentsByStatus(status));
    }
    @GetMapping("/stats")
    public ResponseEntity<GetAgentsUseCase.AgentStatsDto> getStats() {
        return ResponseEntity.ok(getAgentsUseCase.getStats());
    }
}
