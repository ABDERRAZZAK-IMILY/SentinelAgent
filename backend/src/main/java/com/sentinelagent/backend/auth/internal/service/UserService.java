package com.sentinelagent.backend.auth.internal.service;

import com.sentinelagent.backend.auth.dto.UserRequest;
import com.sentinelagent.backend.auth.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAll();

    UserResponse create(UserRequest request);

    void delete(String userId);
}

