package com.sentinelagent.backend.agent;

import com.sentinelagent.backend.agent.*;
import com.sentinelagent.backend.agent.dto.*;
import com.sentinelagent.backend.agent.internal.infrastructure.persistence.AgentCommandDocument;
import com.sentinelagent.backend.agent.internal.infrastructure.persistence.SpringDataAgentCommandRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final RegisterAgentUseCase registerAgentUseCase;
    private final ProcessHeartbeatUseCase processHeartbeatUseCase;
    private final GetAgentsUseCase getAgentsUseCase;
    private final SpringDataAgentCommandRepository commandRepository;
    private final AgentCommandDispatchService agentCommandDispatchService;

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

    @PostMapping("/{agentId}/commands")
    public ResponseEntity<AgentCommandDocument> sendCommand(
            @PathVariable String agentId,
            @RequestBody Map<String, String> payload) {

        String command = payload.get("command");
        String parameters = payload.getOrDefault("parameters", "{}");

        var doc = AgentCommandDocument.builder()
                .agentId(agentId)
                .command(command)
                .parameters(parameters)
                .status("PENDING")
                .issuedAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(commandRepository.save(doc));
    }

    @GetMapping("/{agentId}/commands/pending")
    public ResponseEntity<List<AgentCommandDocument>> getPendingCommands(
            @PathVariable String agentId,
            @RequestHeader("X-Agent-Key") String apiKey) {

        return ResponseEntity.ok(agentCommandDispatchService.getPendingCommands(agentId, apiKey));
    }

    @PutMapping("/{agentId}/commands/{commandId}/result")
    public ResponseEntity<AgentCommandDocument> updateCommandResult(
            @PathVariable String agentId,
            @PathVariable String commandId,
            @RequestHeader("X-Agent-Key") String apiKey,
            @RequestBody AgentCommandResultRequest request) {

        AgentCommandDocument updated = agentCommandDispatchService.updateCommandResult(
                agentId,
                commandId,
                apiKey,
                request.status(),
                request.resultMessage());

        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{agentId}/commands/history")
    public ResponseEntity<List<AgentCommandDocument>> getCommandHistory(@PathVariable String agentId) {
        return ResponseEntity.ok(commandRepository.findByAgentIdOrderByIssuedAtDesc(agentId));
    }

    public record AgentCommandResultRequest(String status, String resultMessage) {
    }
}
