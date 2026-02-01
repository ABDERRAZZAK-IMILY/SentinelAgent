package com.sentinelagent.backend.application.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for iris-based login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrisLoginRequest {
    private String username;
    private String apiKey;
}
