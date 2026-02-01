package com.sentinelagent.backend.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.sentinelagent.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @deprecated Use
 *             {@link com.sentinelagent.backend.infrastructure.security.JwtService}
 *             instead.
 *             This class delegates to the new JwtService for backward
 *             compatibility.
 */
@Deprecated(forRemoval = true)
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtService jwtService;

    public String generateToken(String username, List<String> roles) {
        return jwtService.generateToken(username, roles);
    }

    public DecodedJWT validateToken(String token) {
        return jwtService.validateToken(token);
    }

    public String getUsername(DecodedJWT jwt) {
        return jwtService.getUsername(jwt);
    }
}