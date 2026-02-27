package com.sentinelagent.backend.auth.internal.infrastructure.persistence;

import com.sentinelagent.backend.auth.internal.domain.User;
import com.sentinelagent.backend.auth.internal.domain.UserId;
import com.sentinelagent.backend.auth.internal.infrastructure.persistence.UserDocument;
import org.springframework.stereotype.Component;


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
