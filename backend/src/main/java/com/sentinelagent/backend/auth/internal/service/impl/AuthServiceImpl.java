package com.sentinelagent.backend.auth.internal.service.impl;

import com.sentinelagent.backend.auth.api.TokenService;
import com.sentinelagent.backend.auth.dto.LoginRequest;
import com.sentinelagent.backend.auth.dto.LoginResponse;
import com.sentinelagent.backend.auth.internal.domain.User;
import com.sentinelagent.backend.auth.internal.domain.UserRepository;
import com.sentinelagent.backend.auth.internal.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()));

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = tokenService.generateToken(user.getUsername(), user.getRoles());

        return LoginResponse.builder()
                .token(token)
                .build();
    }

    @Override
    public SetupResult setupAdmin() {
        var existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            String storedPassword = existingAdmin.get().getPassword();
            if (storedPassword == null || !storedPassword.startsWith("$2")) {
                User admin = existingAdmin.get();
                admin.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
                return SetupResult.success("Admin password re-encoded successfully");
            }
            return SetupResult.failure("Admin already exists");
        }

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .roles(List.of("ROLE_ADMIN"))
                .build();

        userRepository.save(admin);
        return SetupResult.success("Admin created successfully");
    }
}

