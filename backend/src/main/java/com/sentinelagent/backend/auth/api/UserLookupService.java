package com.sentinelagent.backend.auth.api;

import java.util.List;
import java.util.Optional;

public interface UserLookupService {
    Optional<UserInfo> findByUsername(String username);
    record UserInfo(String username, String password, List<String> roles) {
    }
}
