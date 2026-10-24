package com.davidrr.grindprotocol.user.adapter;

import com.davidrr.grindprotocol.user.exception.UserAlreadyExistsException;
import com.davidrr.grindprotocol.user.model.User;
import com.davidrr.grindprotocol.user.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JpaUserRegistrationPortTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final JpaUserRegistrationPort port = new JpaUserRegistrationPort(repository);

    @Test
    void duplicateUsernameIsRejectedBeforeSaving() {
        when(repository.existsByUsername("taken")).thenReturn(true);
        assertConflict(() -> port.register("taken", "new@example.test", "encoded-secret"));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void duplicateEmailIsRejectedBeforeSaving() {
        when(repository.existsByEmail("taken@example.test")).thenReturn(true);
        assertConflict(() -> port.register("new", "taken@example.test", "encoded-secret"));
        verify(repository).existsByUsername("new");
        verify(repository, never()).saveAndFlush(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"uk_users_username", "uk_users_email"})
    void concurrentKnownConstraintIsTranslated(String constraint) {
        when(repository.saveAndFlush(any())).thenThrow(violation(constraint));
        assertConflict(() -> port.register("new", "new@example.test", "encoded-secret"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"uk_other_email", "uk_users_username_extra", "UK_USERS_EMAIL"})
    void otherOrUnknownConstraintsRemainTechnicalErrors(String constraint) {
        var failure = violation(constraint);
        when(repository.saveAndFlush(any())).thenThrow(failure);
        assertThatThrownBy(() -> port.register("new", "new@example.test", "encoded-secret"))
                .isSameAs(failure);
    }

    @Test
    void unclassifiedIntegrityErrorIsNotAConflict() {
        var failure = new DataIntegrityViolationException("not a unique violation");
        when(repository.saveAndFlush(any())).thenThrow(failure);
        assertThatThrownBy(() -> port.register("new", "new@example.test", "encoded-secret"))
                .isSameAs(failure);
    }

    @Test
    void validRegistrationPreservesUserFieldsAndReturnsSavedIdentity() {
        when(repository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertThat(user.getUsername()).isEqualTo("new");
            assertThat(user.getEmail()).isEqualTo("new@example.test");
            assertThat(user.getPassword()).isEqualTo("encoded-secret");
            assertThat(user.getRole()).isEqualTo("USER");
            assertThat(user.isEnabled()).isTrue();
            assertThat(user.isEmailVerified()).isFalse();
            user.setId(42L);
            return user;
        });
        var result = port.register("new", "new@example.test", "encoded-secret");
        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.username()).isEqualTo("new");
        verify(repository).existsByUsername("new");
        verify(repository).existsByEmail("new@example.test");
    }

    private void assertConflict(org.assertj.core.api.ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action).isInstanceOfSatisfying(UserAlreadyExistsException.class, ex -> {
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getCode()).isEqualTo("USER_ALREADY_EXISTS");
            assertThat(ex.getMessage()).isEqualTo(
                    "Ya existe un usuario con ese nombre de usuario o correo electrónico");
            assertThat(ex.getCause()).isNull();
        });
    }

    public static DataIntegrityViolationException violation(String constraint) {
        return new DataIntegrityViolationException("sensitive SQL detail",
                new RuntimeException(new ConstraintViolationException("sensitive SQL detail",
                        new SQLException("sensitive value", "23505"), constraint)));
    }
}
