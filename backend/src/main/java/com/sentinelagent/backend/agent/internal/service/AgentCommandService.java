package com.sentinelagent.backend.agent.internal.service;

import com.sentinelagent.backend.agent.dto.AgentCommandResultRequest;
import com.sentinelagent.backend.agent.dto.SendCommandRequest;
import com.sentinelagent.backend.agent.internal.domain.AgentCommandDocument;

import java.util.List;

public interface AgentCommandService {
    void issueCommand(String agentId, SendCommandRequest request);
    List<AgentCommandDocument> getPendingCommands(String agentId, String apiKey);
    AgentCommandDocument updateCommandResult(String agentId, String commandId, String apiKey, AgentCommandResultRequest request);
    List<AgentCommandDocument> getCommandHistory(String agentId);
}

