package com.sentinelagent.backend.controller;

import com.sentinelagent.backend.model.User;
import com.sentinelagent.backend.repository.UserRepository;
import com.sentinelagent.backend.security.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${iris.service.secret}")
    private String irisServiceSecret;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setupAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(List.of("ROLE_ADMIN"));
            userRepository.save(admin);
            return ResponseEntity.ok("Admin created successfully");
        }
        return ResponseEntity.badRequest().body("Admin already exists");
    }



    @PostMapping("/iris-login")
    public ResponseEntity<?> loginWithIris(@RequestBody IrisLoginRequest request) {
        if (!irisServiceSecret.equals(request.getApiKey())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Service Key");
        }

        // Find existing user or create new one (iris verification is trusted)
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(request.getUsername());
                    // Set a random password since iris users don't need password auth
                    newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    newUser.setRoles(List.of("ROLE_USER"));
                    return userRepository.save(newUser);
                });

        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}

@Data
class IrisLoginRequest {
    private String username;
    private String apiKey;
}

@Data
class LoginRequest {
    private String username;
    private String password;
}

@Data
class LoginResponse {
    private String token;
    public LoginResponse(String token) { this.token = token; }
}