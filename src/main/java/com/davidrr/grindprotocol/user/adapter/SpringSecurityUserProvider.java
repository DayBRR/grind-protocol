package com.davidrr.grindprotocol.user.adapter;

import com.davidrr.security.auth.port.UserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringSecurityUserProvider implements UserProvider {

    private final UserDetailsService userDetailsService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userDetailsService.loadUserByUsername(username);
    }
}