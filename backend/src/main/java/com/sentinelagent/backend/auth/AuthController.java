package com.sentinelagent.backend.auth;

import com.sentinelagent.backend.auth.*;
import com.sentinelagent.backend.auth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final IrisLoginUseCase irisLoginUseCase;
    private final SetupAdminUseCase setupAdminUseCase;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginUseCase.execute(request);
        return ResponseEntity.ok(response);
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
            LoginResponse response = irisLoginUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (IrisLoginUseCase.InvalidServiceKeyException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Service Key");
        }
    }
}
