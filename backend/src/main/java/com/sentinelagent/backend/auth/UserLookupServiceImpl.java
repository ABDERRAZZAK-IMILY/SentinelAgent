package com.sentinelagent.backend.auth;

import com.sentinelagent.backend.auth.internal.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLookupServiceImpl implements UserLookupService {

    private final UserRepository userRepository;

    @Override
    public Optional<UserInfo> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> new UserInfo(
                        user.getUsername(),
                        user.getPassword(),
                        user.getRoles()));
    }
}
