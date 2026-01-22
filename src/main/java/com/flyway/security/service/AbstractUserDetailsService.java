package com.flyway.security.service;

import com.flyway.security.principal.CustomUserDetails;
import com.flyway.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public abstract class AbstractUserDetailsService implements UserDetailsService {

    @Override
    public final UserDetails loadUserByUsername(String key) throws UsernameNotFoundException {
        if (key == null || key.isBlank()) {
            throw new UsernameNotFoundException("Login key is blank");
        }

        User user = findUser(key.trim());
        if (user == null) {
            throw new UsernameNotFoundException(notFoundMessage(key));
        }
        return new CustomUserDetails(user);
    }

    protected abstract User findUser(String key);

    protected abstract String notFoundMessage(String key);
}
