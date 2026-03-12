package com.sentinelagent.backend.auth;

import com.sentinelagent.backend.auth.internal.infrastructure.persistence.SpringDataUserRepository;
import com.sentinelagent.backend.auth.internal.infrastructure.persistence.UserDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SpringDataUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<UserDocument>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDocument newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // Ensure default role if none provided
        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles(List.of("ANALYST"));
        }

        UserDocument savedUser = userRepository.save(newUser);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.ok().build();
    }
}
