package com.sentinelagent.backend.domain.user;

import lombok.*;

import java.util.List;

/**
 * Domain Entity representing a system user.
 * Part of the Domain Layer - no external dependencies.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    private UserId id;
    private String username;
    private String password;
    private List<String> roles;

    /**
     * Check if user has admin role
     */
    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
