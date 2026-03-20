package com.sentinelagent.backend.agent.internal.controller;

import com.sentinelagent.backend.agent.dto.AgentCommandResultRequest;
import com.sentinelagent.backend.agent.dto.AgentDetailsDto;
import com.sentinelagent.backend.agent.dto.AgentRegistrationRequest;
import com.sentinelagent.backend.agent.dto.AgentRegistrationResponse;
import com.sentinelagent.backend.agent.dto.HeartbeatRequest;
import com.sentinelagent.backend.agent.dto.SendCommandRequest;
import com.sentinelagent.backend.agent.internal.domain.AgentCommandDocument;
import com.sentinelagent.backend.agent.internal.service.AgentCommandService;
import com.sentinelagent.backend.agent.internal.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final AgentCommandService commandService;

    @PostMapping("/register")
    public ResponseEntity<AgentRegistrationResponse> registerAgent(
            @Valid @RequestBody AgentRegistrationRequest request) {
        AgentRegistrationResponse response = agentService.registerAgent(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @RequestHeader("X-Agent-Key") String apiKey,
            @Valid @RequestBody HeartbeatRequest request) {
        agentService.processHeartbeat(apiKey, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<AgentDetailsDto>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<AgentDetailsDto> getAgentById(@PathVariable("agentId") String agentId) {
        return ResponseEntity.ok(agentService.getById(agentId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AgentDetailsDto>> getAgentsByStatus(@PathVariable("status") String status) {
        return ResponseEntity.ok(agentService.getAgentsByStatus(status));
    }

    @GetMapping("/stats")
    public ResponseEntity<AgentService.AgentStatsDto> getStats() {
        return ResponseEntity.ok(agentService.getStats());
    }

    @PostMapping("/{agentId}/commands")
    public ResponseEntity<Void> sendCommand(
            @PathVariable("agentId") String agentId,
            @RequestBody SendCommandRequest request) {
        commandService.issueCommand(agentId, request);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/{agentId}/commands/pending")
    public ResponseEntity<List<AgentCommandDocument>> getPendingCommands(
            @PathVariable("agentId") String agentId,
            @RequestHeader("X-Agent-Key") String apiKey) {
        return ResponseEntity.ok(commandService.getPendingCommands(agentId, apiKey));
    }

    @PutMapping("/{agentId}/commands/{commandId}/result")
    public ResponseEntity<AgentCommandDocument> updateCommandResult(
            @PathVariable("agentId") String agentId,
            @PathVariable("commandId") String commandId,
            @RequestHeader("X-Agent-Key") String apiKey,
            @RequestBody AgentCommandResultRequest request) {
        AgentCommandDocument updated = commandService.updateCommandResult(agentId, commandId, apiKey, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{agentId}/commands/history")
    public ResponseEntity<List<AgentCommandDocument>> getCommandHistory(@PathVariable("agentId") String agentId) {
        return ResponseEntity.ok(commandService.getCommandHistory(agentId));
    }
}
