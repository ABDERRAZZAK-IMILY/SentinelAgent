package com.sentinelagent.backend.auth.api;

import java.util.List;

public interface TokenService {

    String generateToken(String username, List<String> roles);
}
