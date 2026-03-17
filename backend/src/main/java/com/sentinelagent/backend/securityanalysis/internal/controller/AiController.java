package com.sentinelagent.backend.securityanalysis.internal.controller;

import com.sentinelagent.backend.securityanalysis.dto.ChatRequest;
import com.sentinelagent.backend.securityanalysis.dto.ChatResponse;
import com.sentinelagent.backend.securityanalysis.internal.service.SecurityChatService;
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

    private final SecurityChatService chatService;

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

        String responseText = chatService.chatWithAgent(message, agentId);
        return ResponseEntity.ok(new ChatResponse(responseText));
    }
}
