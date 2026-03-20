package com.sentinelagent.backend.auth.internal.service;

import com.sentinelagent.backend.auth.dto.LoginRequest;
import com.sentinelagent.backend.auth.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    SetupResult setupAdmin();

    record SetupResult(boolean success, String message) {
        public static SetupResult success(String message) {
            return new SetupResult(true, message);
        }

        public static SetupResult failure(String message) {
            return new SetupResult(false, message);
        }
    }
}

