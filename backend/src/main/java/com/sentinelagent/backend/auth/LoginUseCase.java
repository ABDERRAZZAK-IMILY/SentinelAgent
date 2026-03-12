package com.sentinelagent.backend.auth;

import com.sentinelagent.backend.auth.dto.LoginRequest;
import com.sentinelagent.backend.auth.dto.LoginResponse;
import com.sentinelagent.backend.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final com.sentinelagent.backend.auth.internal.domain.UserRepository userRepository;

    public LoginResponse execute(LoginRequest request) {
        // Authenticate user credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        // Find user and generate token
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = tokenService.generateToken(user.getUsername(), user.getRoles());

        return LoginResponse.builder()
                .token(token)
                .build();
    }
}
