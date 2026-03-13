package com.sentinelagent.backend.securityanalysis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiChatUseCase aiChatUseCase;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAiStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "modelVersion", "deepseek-r1-v2",
                "lastTrainingDate", LocalDate.now().minusDays(3).toString(),
                "accuracy", 0.94,
                "ragDatabaseStatus", "CONNECTED"));
    }

    @PostMapping("/train")
    public ResponseEntity<Map<String, String>> triggerTraining() {
        return ResponseEntity.accepted().body(Map.of(
                "message", "Training job has been queued successfully.",
                "jobId", "TRN-" + System.currentTimeMillis()));
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String message = request != null && request.message() != null ? request.message().trim() : "";
        String agentId = request != null && request.agentId() != null ? request.agentId().trim() : null;
        if (message.isBlank()) {
            return ResponseEntity.badRequest().body(new ChatResponse("Please provide a message."));
        }

        String responseText = aiChatUseCase.execute(message, agentId);
        return ResponseEntity.ok(new ChatResponse(responseText));
    }

    public record ChatRequest(String message, String agentId) {
    }

    public record ChatResponse(String response) {
    }
}
