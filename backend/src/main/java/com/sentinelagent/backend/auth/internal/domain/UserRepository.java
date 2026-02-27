package com.sentinelagent.backend.auth.internal.domain;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAll();

    void deleteById(UserId id);

    long count();
}
