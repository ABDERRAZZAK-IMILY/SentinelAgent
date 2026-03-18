package com.sentinelagent.backend.agent.internal.service;

import com.sentinelagent.backend.agent.AgentNotFoundException;
import com.sentinelagent.backend.agent.api.InvalidAgentCredentialsException;
import com.sentinelagent.backend.agent.dto.AgentCommandResultRequest;
import com.sentinelagent.backend.agent.dto.SendCommandRequest;
import com.sentinelagent.backend.agent.internal.domain.AgentCommandDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.repository.SpringDataAgentCommandRepository;
import com.sentinelagent.backend.agent.internal.repository.SpringDataAgentRepository;
import com.sentinelagent.backend.agent.internal.security.ApiKeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentCommandServiceImplTest {

    @Mock
    private SpringDataAgentRepository agentRepository;
    @Mock
    private SpringDataAgentCommandRepository commandRepository;
    @Mock
    private ApiKeyService apiKeyService;

    @InjectMocks
    private AgentCommandServiceImpl commandService;

    @Test
    void issueCommand_persistsPendingCommandWithDefaults() {
        AgentDocument agent = AgentDocument.builder().id("agent-1").build();
        SendCommandRequest request = SendCommandRequest.builder()
                .command("RUN")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(commandRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        commandService.issueCommand("agent-1", request);

        ArgumentCaptor<AgentCommandDocument> captor = ArgumentCaptor.forClass(AgentCommandDocument.class);
        verify(commandRepository).save(captor.capture());
        AgentCommandDocument saved = captor.getValue();
        assertEquals("agent-1", saved.getAgentId());
        assertEquals("RUN", saved.getCommand());
        assertEquals("{}", saved.getParameters());
        assertEquals("PENDING", saved.getStatus());
        assertNotNull(saved.getIssuedAt());
    }

    @Test
    void issueCommand_whenAgentMissing_throwsException() {
        SendCommandRequest request = SendCommandRequest.builder().command("RUN").build();
        when(agentRepository.findById("agent-1")).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class, () -> commandService.issueCommand("agent-1", request));
        verify(commandRepository, never()).save(any());
    }


    @Test
    void getPendingCommands_withInvalidApiKey_throwsException() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("bad-key", "hash")).thenReturn(false);

        assertThrows(InvalidAgentCredentialsException.class, () -> commandService.getPendingCommands("agent-1", "bad-key"));
        verify(commandRepository, never()).findByAgentIdAndStatusOrderByIssuedAtAsc(any(), any());
        verify(agentRepository, never()).save(any());
    }

    @Test
    void getPendingCommands_whenAgentRevoked_throwsException() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.REVOKED.name())
                .apiKeyHash("hash")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);

        assertThrows(InvalidAgentCredentialsException.class, () -> commandService.getPendingCommands("agent-1", "plain-key"));
        verify(agentRepository, never()).save(any());
    }

    @Test
    void updateCommandResult_updatesStatusAndTimestamp() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .build();
        AgentCommandDocument command = AgentCommandDocument.builder()
                .id("cmd-1")
                .agentId("agent-1")
                .status("PENDING")
                .build();
        AgentCommandResultRequest request = AgentCommandResultRequest.builder()
                .status("success")
                .resultMessage("done")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);
        when(commandRepository.findById("cmd-1")).thenReturn(Optional.of(command));
        when(commandRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AgentCommandDocument result = commandService.updateCommandResult("agent-1", "cmd-1", "plain-key", request);

        assertEquals("SUCCESS", result.getStatus());
        assertEquals("done", result.getResultMessage());
        assertNotNull(result.getExecutedAt());
    }

    @Test
    void updateCommandResult_normalizesNullStatusToFailed() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .build();
        AgentCommandDocument command = AgentCommandDocument.builder()
                .id("cmd-1")
                .agentId("agent-1")
                .status("PENDING")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);
        when(commandRepository.findById("cmd-1")).thenReturn(Optional.of(command));
        when(commandRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(agentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AgentCommandDocument result = commandService.updateCommandResult("agent-1", "cmd-1", "plain-key", AgentCommandResultRequest.builder().build());

        assertEquals("FAILED", result.getStatus());
        assertNotNull(result.getExecutedAt());
    }

    @Test
    void updateCommandResult_whenCommandBelongsToAnotherAgent_throwsException() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .build();
        AgentCommandDocument command = AgentCommandDocument.builder()
                .id("cmd-1")
                .agentId("agent-2")
                .status("PENDING")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);
        when(commandRepository.findById("cmd-1")).thenReturn(Optional.of(command));

        assertThrows(InvalidAgentCredentialsException.class, () -> commandService.updateCommandResult("agent-1", "cmd-1", "plain-key", AgentCommandResultRequest.builder().build()));
        verify(commandRepository, never()).save(any());
    }

    @Test
    void updateCommandResult_whenCommandMissing_throwsException() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);
        when(commandRepository.findById("cmd-1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> commandService.updateCommandResult("agent-1", "cmd-1", "plain-key", AgentCommandResultRequest.builder().build()));
    }

    @Test
    void getCommandHistory_delegatesToRepository() {
        List<AgentCommandDocument> history = List.of(AgentCommandDocument.builder().id("cmd-1").build());
        when(commandRepository.findByAgentIdOrderByIssuedAtDesc("agent-1")).thenReturn(history);

        List<AgentCommandDocument> result = commandService.getCommandHistory("agent-1");

        assertEquals(history, result);
    }
}

