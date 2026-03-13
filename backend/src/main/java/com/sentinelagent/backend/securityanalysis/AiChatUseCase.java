package com.sentinelagent.backend.securityanalysis;

import com.sentinelagent.backend.telemetry.TelemetryQueryService;
import com.sentinelagent.backend.telemetry.TelemetryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatUseCase {

    private static final String CHAT_FALLBACK = "AI uplink is temporarily unavailable. Please try again in a moment.";

    private final ChatModel chatModel;
    private final TelemetryQueryService telemetryQueryService;

    public String execute(String message, String agentId) {
        if (message == null || message.isBlank()) {
            return null;
        }

        try {
            String telemetryContext = buildTelemetryContext(agentId);

            PromptTemplate template = new PromptTemplate("""
                    You are Sentinel Brain-Bot, an AI assistant embedded in the SentinelAgent security platform.
                    Your ONLY purpose is to answer questions related to:
                    - Cybersecurity topics (threats, vulnerabilities, incidents, best practices, malware, attacks, etc.)
                    - Agent data and operations (agent status, heartbeats, telemetry, alerts, security events, reports)

                    STRICT RULES:
                    1. If the user's question is NOT related to cybersecurity or agent/platform data, respond ONLY with:
                       "I can only assist with cybersecurity and SentinelAgent platform questions."
                    2. Do NOT answer general knowledge, coding help, math, personal, or any off-topic questions.
                    3. Use the telemetry context when the question is about a specific agent; if telemetry is missing, say so.
                    4. Do NOT fabricate data that is not in telemetry context.
                    5. Keep answers concise, practical, and focused on security operations.
                    6. If the question is about interpreting telemetry, analyze the provided telemetry data to give insights on potential security implications.
                    7. you answer for hi or hello should be "Hello! I'm Sentinel Brain-Bot, your cybersecurity assistant. How can I help you with SentinelAgent or cybersecurity questions today?"
                    Telemetry context:
                    {telemetry}

                    User message: {message}
                    """);

            String responseText = chatModel.call(template.create(Map.of(
                            "message", message,
                            "telemetry", telemetryContext)))
                    .getResult()
                    .getOutput()
                    .getText();

            if (responseText == null || responseText.isBlank()) {
                return CHAT_FALLBACK;
            }

            return responseText.trim();
        } catch (Exception ex) {
            log.error("AI chat call failed", ex);
            return CHAT_FALLBACK;
        }
    }

    private String buildTelemetryContext(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            return "No agentId provided. Telemetry unavailable.";
        }

        Optional<TelemetryResponse> latest = telemetryQueryService.getLatest(agentId);
        if (latest.isEmpty()) {
            return "No telemetry found for agent " + agentId + ".";
        }

        TelemetryResponse t = latest.get();
        return String.format(
                "Agent %s (%s) telemetry @ %s | CPU: %.1f%% | RAM: %.1f%% of %d MB | Disk: %.1f%% of %d GB | Net: %.2f MB/s up, %.2f MB/s down | Processes: %d | Connections: %d",
                t.agentId(),
                t.hostname(),
                t.receivedAt(),
                t.cpuUsage(),
                t.ramUsedPercent(),
                t.ramTotalMb(),
                t.diskUsedPercent(),
                t.diskTotalGb(),
                t.bytesSentSec() / 1024.0 / 1024.0,
                t.bytesRecvSec() / 1024.0 / 1024.0,
                t.processes().size(),
                t.networkConnections().size());
    }
}
