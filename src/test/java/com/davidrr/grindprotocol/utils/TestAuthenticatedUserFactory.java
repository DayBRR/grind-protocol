package com.davidrr.grindprotocol.utils;

import com.davidrr.grindprotocol.security.model.AuthenticatedUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Stream;

public final class TestAuthenticatedUserFactory {

    private TestAuthenticatedUserFactory() {

    }

    public static AuthenticatedUser defaultUser() {
        return new AuthenticatedUser(
                1L,
                "david",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public static AuthenticatedUser withId(Long id) {
        return new AuthenticatedUser(
                id,
                "user_" + id,
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public static AuthenticatedUser withRoles(Long id, String username, String... roles) {
        return new AuthenticatedUser(
                id,
                username,
                "password",
                Stream.of(roles)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}