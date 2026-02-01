package com.sentinelagent.backend.infrastructure.persistence.mapper;

import com.sentinelagent.backend.domain.user.User;
import com.sentinelagent.backend.domain.user.UserId;
import com.sentinelagent.backend.infrastructure.persistence.entity.UserDocument;
import org.springframework.stereotype.Component;

/**
 * Mapper for User domain entity and MongoDB document.
 */
@Component
public class UserMapper {

    public UserDocument toDocument(User user) {
        return UserDocument.builder()
                .id(user.getId() != null ? user.getId().getValue() : null)
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();
    }

    public User toDomain(UserDocument document) {
        return User.builder()
                .id(UserId.of(document.getId()))
                .username(document.getUsername())
                .password(document.getPassword())
                .roles(document.getRoles())
                .build();
    }
}
