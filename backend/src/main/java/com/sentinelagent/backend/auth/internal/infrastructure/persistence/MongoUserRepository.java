package com.sentinelagent.backend.auth.internal.infrastructure.persistence;

import com.sentinelagent.backend.auth.internal.domain.User;
import com.sentinelagent.backend.auth.internal.domain.UserId;
import com.sentinelagent.backend.auth.internal.domain.UserRepository;
import com.sentinelagent.backend.auth.internal.infrastructure.persistence.UserDocument;
import com.sentinelagent.backend.auth.internal.infrastructure.persistence.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class MongoUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataRepository;
    private final UserMapper mapper;

    @Override
    public User save(User user) {
        UserDocument document = mapper.toDocument(user);
        UserDocument saved = springDataRepository.save(document);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return springDataRepository.findById(id.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataRepository.existsByUsername(username);
    }

    @Override
    public List<User> findAll() {
        return springDataRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UserId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
