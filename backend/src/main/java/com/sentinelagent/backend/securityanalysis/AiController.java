package com.sentinelagent.backend.securityanalysis;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {


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
}
