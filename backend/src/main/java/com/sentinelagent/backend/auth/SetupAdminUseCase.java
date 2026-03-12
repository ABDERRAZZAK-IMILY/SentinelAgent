package com.sentinelagent.backend.auth;

import com.sentinelagent.backend.auth.internal.domain.User;
import com.sentinelagent.backend.auth.internal.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SetupAdminUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SetupResult execute() {
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

    public record SetupResult(boolean success, String message) {
        public static SetupResult success(String message) {
            return new SetupResult(true, message);
        }

        public static SetupResult failure(String message) {
            return new SetupResult(false, message);
        }
    }
}
