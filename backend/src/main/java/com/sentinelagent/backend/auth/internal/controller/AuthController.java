package com.sentinelagent.backend.auth.internal.controller;

import com.sentinelagent.backend.auth.dto.LoginRequest;
import com.sentinelagent.backend.auth.dto.LoginResponse;
import com.sentinelagent.backend.auth.internal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/setup")
    public ResponseEntity<String> setupAdmin() {
        AuthService.SetupResult result = authService.setupAdmin();
        return result.success()
                ? ResponseEntity.ok(result.message())
                : ResponseEntity.badRequest().body(result.message());
    }
}
