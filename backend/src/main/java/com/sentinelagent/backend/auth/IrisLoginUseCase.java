package com.sentinelagent.backend.auth;

import com.sentinelagent.backend.auth.dto.IrisLoginRequest;
import com.sentinelagent.backend.auth.dto.LoginResponse;
import com.sentinelagent.backend.auth.internal.domain.User;
import com.sentinelagent.backend.auth.internal.domain.UserRepository;
import com.sentinelagent.backend.auth.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class IrisLoginUseCase {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${iris.service.secret}")
    private String irisServiceSecret;

    public LoginResponse execute(IrisLoginRequest request) {
        // Validate iris service API key
        if (!irisServiceSecret.equals(request.getApiKey())) {
            throw new InvalidServiceKeyException("Invalid Service Key");
        }

        // Find or create user
        User user = userRepository.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(request.getUsername())
                            .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                            .roles(List.of("ROLE_USER"))
                            .build();
                    return userRepository.save(newUser);
                });

        String token = tokenService.generateToken(user.getUsername(), user.getRoles());

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    public static class InvalidServiceKeyException extends RuntimeException {
        public InvalidServiceKeyException(String message) {
            super(message);
        }
    }
}
