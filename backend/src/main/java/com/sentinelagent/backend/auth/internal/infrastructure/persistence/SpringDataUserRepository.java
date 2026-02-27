package com.sentinelagent.backend.auth.internal.infrastructure.persistence;

import com.sentinelagent.backend.auth.internal.infrastructure.persistence.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;


public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsername(String username);

    boolean existsByUsername(String username);
}
