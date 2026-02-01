package com.sentinelagent.backend.security;

import com.sentinelagent.backend.infrastructure.security.JwtAuthFilter;
import com.sentinelagent.backend.infrastructure.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @deprecated Use
 *             {@link com.sentinelagent.backend.infrastructure.security.JwtAuthFilter}
 *             instead.
 *             This class delegates to the new JwtAuthFilter for backward
 *             compatibility.
 */
@Deprecated(forRemoval = true)
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthFilter delegateFilter;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.delegateFilter = new JwtAuthFilter(jwtService);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        delegateFilter.doFilter(request, response, filterChain);
    }
}