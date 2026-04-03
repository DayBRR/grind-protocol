package com.davidrr.grindprotocol.user.adapter;

import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.security.auth.port.UserRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRegistrationPort implements UserRegistrationPort {

    private final UserRepository userRepository;

    @Override
    public RegisteredUser register(String username, String encodedPassword) {
        userRepository.findByUsername(username)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Username already exists: " + username);
                });

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .role("USER")
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        return new RegisteredUser(savedUser.getId(), savedUser.getUsername());
    }
}