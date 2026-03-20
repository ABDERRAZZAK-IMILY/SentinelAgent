package com.sentinelagent.backend.auth.internal.service.impl;

import com.sentinelagent.backend.auth.dto.UserRequest;
import com.sentinelagent.backend.auth.dto.UserResponse;
import com.sentinelagent.backend.auth.internal.domain.UserId;
import com.sentinelagent.backend.auth.internal.domain.UserRepository;
import com.sentinelagent.backend.auth.internal.mapper.UserDtoMapper;
import com.sentinelagent.backend.auth.internal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserDtoMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        var user = mapper.toDomain(request, encodedPassword);
        var saved = userRepository.save(user);
        return mapper.toResponse(saved);
    }

    @Override
    public void delete(String userId) {
        var id = UserId.of(userId);
        userRepository.deleteById(id);
    }
}

