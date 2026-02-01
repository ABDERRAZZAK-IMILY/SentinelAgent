package com.sentinelagent.backend.controller;

import com.sentinelagent.backend.application.auth.*;
import com.sentinelagent.backend.application.auth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @deprecated Use {@link com.sentinelagent.backend.api.v1.auth.AuthController}
 *             instead.
 *             This class delegates to the new auth use cases for backward
 *             compatibility.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping("/api/auth-legacy")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final IrisLoginUseCase irisLoginUseCase;
    private final SetupAdminUseCase setupAdminUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    @PostMapping("/setup")
    public ResponseEntity<String> setupAdmin() {
        SetupAdminUseCase.SetupResult result = setupAdminUseCase.execute();
        if (result.success()) {
            return ResponseEntity.ok(result.message());
        }
        return ResponseEntity.badRequest().body(result.message());
    }

    @PostMapping("/iris-login")
    public ResponseEntity<?> loginWithIris(@RequestBody IrisLoginRequest request) {
        try {
            return ResponseEntity.ok(irisLoginUseCase.execute(request));
        } catch (IrisLoginUseCase.InvalidServiceKeyException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Service Key");
        }
    }
}