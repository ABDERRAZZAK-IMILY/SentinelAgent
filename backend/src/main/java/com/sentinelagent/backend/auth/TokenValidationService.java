package com.sentinelagent.backend.auth;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.List;


public interface TokenValidationService {
    DecodedJWT validateToken(String token);

    String getUsername(DecodedJWT jwt);
    List<String> getRoles(DecodedJWT jwt);
}
