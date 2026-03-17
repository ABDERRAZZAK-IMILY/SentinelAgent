package com.sentinelagent.backend.agent.internal.service;

import com.sentinelagent.backend.agent.AgentAlreadyExistsException;
import com.sentinelagent.backend.agent.AgentNotFoundException;
import com.sentinelagent.backend.agent.api.InvalidAgentCredentialsException;
import com.sentinelagent.backend.agent.dto.AgentDetailsDto;
import com.sentinelagent.backend.agent.dto.AgentRegistrationRequest;
import com.sentinelagent.backend.agent.dto.HeartbeatRequest;
import com.sentinelagent.backend.agent.internal.domain.AgentDocument;
import com.sentinelagent.backend.agent.internal.domain.AgentStatus;
import com.sentinelagent.backend.agent.internal.mapper.AgentMapper;
import com.sentinelagent.backend.agent.internal.repository.SpringDataAgentRepository;
import com.sentinelagent.backend.agent.internal.security.ApiKeyService;
import com.sentinelagent.backend.agent.internal.service.AgentService.AgentStatsDto;
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
class AgentServiceImplTest {

    @Mock
    private SpringDataAgentRepository agentRepository;
    @Mock
    private ApiKeyService apiKeyService;
    @Mock
    private AgentMapper mapper;

    @InjectMocks
    private AgentServiceImpl agentService;

    @Test
    void registerAgent_savesNewAgentAndReturnsResponse() {
        AgentRegistrationRequest request = AgentRegistrationRequest.builder()
                .hostname("host-1")
                .operatingSystem("Linux")
                .agentVersion("1.0")
                .ipAddress("10.0.0.1")
                .build();

        when(agentRepository.existsByHostname(request.getHostname())).thenReturn(false);
        when(apiKeyService.generateApiKey()).thenReturn("plain-key");
        when(apiKeyService.hashApiKey("plain-key")).thenReturn("hashed-key");
        when(agentRepository.save(any())).thenAnswer(invocation -> {
            AgentDocument doc = invocation.getArgument(0);
            doc.setId("agent-id-1");
            return doc;
        });

        var response = agentService.registerAgent(request);

        assertEquals("agent-id-1", response.getAgentId());
        assertEquals("plain-key", response.getApiKey());
        assertEquals(AgentStatus.ACTIVE.name(), response.getStatus());
        assertTrue(response.getMessage().contains("Agent registered successfully"));

        ArgumentCaptor<AgentDocument> captor = ArgumentCaptor.forClass(AgentDocument.class);
        verify(agentRepository).save(captor.capture());
        AgentDocument saved = captor.getValue();
        assertEquals(request.getHostname(), saved.getHostname());
        assertEquals(request.getOperatingSystem(), saved.getOperatingSystem());
        assertEquals(request.getAgentVersion(), saved.getAgentVersion());
        assertEquals(request.getIpAddress(), saved.getIpAddress());
        assertEquals("hashed-key", saved.getApiKeyHash());
        assertEquals(AgentStatus.ACTIVE.name(), saved.getStatus());
        assertNotNull(saved.getRegisteredAt());
        assertNotNull(saved.getLastHeartbeat());
    }

    @Test
    void registerAgent_whenHostnameExists_throwsException() {
        AgentRegistrationRequest request = AgentRegistrationRequest.builder()
                .hostname("host-1")
                .operatingSystem("Linux")
                .agentVersion("1.0")
                .build();

        when(agentRepository.existsByHostname(request.getHostname())).thenReturn(true);

        assertThrows(AgentAlreadyExistsException.class, () -> agentService.registerAgent(request));
        verify(agentRepository, never()).save(any());
    }

    @Test
    void processHeartbeat_updatesTimestampForActiveAgent() {
        LocalDateTime originalHeartbeat = LocalDateTime.now().minusMinutes(5);
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .hostname("host-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .lastHeartbeat(originalHeartbeat)
                .build();

        HeartbeatRequest request = HeartbeatRequest.builder()
                .agentId(agent.getId())
                .status(AgentStatus.ACTIVE.name())
                .build();

        when(agentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);
        when(agentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        agentService.processHeartbeat("plain-key", request);

        ArgumentCaptor<AgentDocument> captor = ArgumentCaptor.forClass(AgentDocument.class);
        verify(agentRepository).save(captor.capture());
        AgentDocument saved = captor.getValue();
        assertTrue(saved.getLastHeartbeat().isAfter(originalHeartbeat));
        assertEquals(AgentStatus.ACTIVE.name(), saved.getStatus());
    }

    @Test
    void processHeartbeat_reactivatesInactiveAgent() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.INACTIVE.name())
                .apiKeyHash("hash")
                .lastHeartbeat(LocalDateTime.now().minusDays(1))
                .build();
        HeartbeatRequest request = HeartbeatRequest.builder().agentId(agent.getId()).build();

        when(agentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);
        when(agentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        agentService.processHeartbeat("plain-key", request);

        ArgumentCaptor<AgentDocument> captor = ArgumentCaptor.forClass(AgentDocument.class);
        verify(agentRepository).save(captor.capture());
        AgentDocument saved = captor.getValue();
        assertEquals(AgentStatus.ACTIVE.name(), saved.getStatus());
        assertNotNull(saved.getLastHeartbeat());
    }

    @Test
    void processHeartbeat_withInvalidApiKey_throwsException() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.ACTIVE.name())
                .apiKeyHash("hash")
                .build();
        HeartbeatRequest request = HeartbeatRequest.builder().agentId(agent.getId()).build();

        when(agentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("bad-key", "hash")).thenReturn(false);

        assertThrows(InvalidAgentCredentialsException.class, () -> agentService.processHeartbeat("bad-key", request));
        verify(agentRepository, never()).save(any());
    }

    @Test
    void processHeartbeat_whenAgentRevoked_throwsException() {
        AgentDocument agent = AgentDocument.builder()
                .id("agent-1")
                .status(AgentStatus.REVOKED.name())
                .apiKeyHash("hash")
                .build();
        HeartbeatRequest request = HeartbeatRequest.builder().agentId(agent.getId()).build();

        when(agentRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(apiKeyService.validateApiKey("plain-key", "hash")).thenReturn(true);

        assertThrows(InvalidAgentCredentialsException.class, () -> agentService.processHeartbeat("plain-key", request));
        verify(agentRepository, never()).save(any());
    }

    @Test
    void processHeartbeat_whenAgentMissing_throwsException() {
        HeartbeatRequest request = HeartbeatRequest.builder().agentId("missing-id").build();
        when(agentRepository.findById(request.getAgentId())).thenReturn(Optional.empty());

        assertThrows(AgentNotFoundException.class, () -> agentService.processHeartbeat("api-key", request));
    }

    @Test
    void getById_returnsMappedDto() {
        AgentDocument agent = AgentDocument.builder().id("agent-1").hostname("host-1").build();
        AgentDetailsDto dto = AgentDetailsDto.builder().agentId("agent-1").hostname("host-1").build();

        when(agentRepository.findById("agent-1")).thenReturn(Optional.of(agent));
        when(mapper.toDetailsDto(agent)).thenReturn(dto);

        AgentDetailsDto result = agentService.getById("agent-1");
        assertEquals(dto, result);
    }

    @Test
    void getStats_returnsAggregatedCounts() {
        when(agentRepository.countByStatus(AgentStatus.ACTIVE.name())).thenReturn(3L);
        when(agentRepository.countByStatus(AgentStatus.INACTIVE.name())).thenReturn(2L);
        when(agentRepository.countByStatus(AgentStatus.REVOKED.name())).thenReturn(1L);
        when(agentRepository.countByStatus(AgentStatus.ERROR.name())).thenReturn(4L);

        AgentStatsDto stats = agentService.getStats();

        assertEquals(3L, stats.active());
        assertEquals(2L, stats.inactive());
        assertEquals(1L, stats.revoked());
        assertEquals(4L, stats.error());
    }
}
