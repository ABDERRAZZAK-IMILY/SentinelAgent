package com.sentinelagent.backend.application.auth;

import com.sentinelagent.backend.application.auth.dto.LoginRequest;
import com.sentinelagent.backend.application.auth.dto.LoginResponse;
import com.sentinelagent.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Use Case for user login authentication.
 * Part of the Application Layer.
 */
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final com.sentinelagent.backend.domain.user.UserRepository userRepository;

    public LoginResponse execute(LoginRequest request) {
        // Authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Find user and generate token
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user.getUsername(), user.getRoles());

        return LoginResponse.builder()
                .token(token)
                .build();
    }
}
