package com.sentinelagent.backend.auth.internal.domain;

import lombok.*;

import java.util.List;


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

    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
}
