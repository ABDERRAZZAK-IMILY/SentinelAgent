package com.sentinelagent.backend.auth.internal.mapper;

import com.sentinelagent.backend.auth.dto.UserRequest;
import com.sentinelagent.backend.auth.dto.UserResponse;
import com.sentinelagent.backend.auth.internal.domain.User;
import com.sentinelagent.backend.auth.internal.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDtoMapper {

    public User toDomain(UserRequest request, String encodedPassword) {
        List<String> roles = (request.getRoles() == null || request.getRoles().isEmpty())
                ? List.of("ANALYST")
                : request.getRoles();

        return User.builder()
                .id(UserId.generate())
                .username(request.getUsername())
                .password(encodedPassword)
                .roles(roles)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId() != null ? user.getId().getValue() : null)
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }
}

