package com.sentinelagent.backend.infrastructure.persistence.repository;

import com.sentinelagent.backend.infrastructure.persistence.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB Repository for User documents.
 */
public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsername(String username);

    boolean existsByUsername(String username);
}
