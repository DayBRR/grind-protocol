package com.davidrr.grindprotocol.user.adapter;

import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.security.auth.port.UserLoginAuditPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class JpaUserLoginAuditPort implements UserLoginAuditPort {

    private final UserRepository userRepository;

    public JpaUserLoginAuditPort(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void recordSuccessfulLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}