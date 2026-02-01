package com.sentinelagent.backend.domain.user;

import java.util.List;
import java.util.Optional;

/**
 * Repository Port for User persistence.
 * Part of the Domain Layer - defines the contract for infrastructure
 * implementation.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAll();

    void deleteById(UserId id);

    long count();
}
