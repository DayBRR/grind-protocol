package com.davidrr.grindprotocol.user.adapter;

import com.davidrr.grindprotocol.user.exception.UserAlreadyExistsException;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import com.davidrr.security.auth.port.UserRegistrationPort;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaUserRegistrationPort implements UserRegistrationPort {

    private final UserRepository userRepository;

    @Override
    public RegisteredUser register(String username, String email, String encodedPassword) {
        if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }

        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .role("USER")
                .email(email)
                .enabled(true)
                .emailVerified(false)
                .build();

        User savedUser;
        try {
            // Surface concurrent uniqueness conflicts before registration events or tokens.
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateUser(ex)) {
                // Do not retain a cause containing SQL parameters or account identifiers.
                throw new UserAlreadyExistsException();
            }
            throw ex;
        }

        return new RegisteredUser(savedUser.getId(), savedUser.getUsername());
    }

    private boolean isDuplicateUser(DataIntegrityViolationException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraint = violation.getConstraintName();
                return "uk_users_username".equals(constraint) || "uk_users_email".equals(constraint);
            }
        }
        return false;
    }
}
