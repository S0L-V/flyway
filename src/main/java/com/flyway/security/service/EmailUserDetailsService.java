package com.flyway.security.service;

import com.flyway.user.domain.User;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("emailUserDetailsService")
@RequiredArgsConstructor
public class EmailUserDetailsService extends AbstractUserDetailsService {

    private final UserRepository userRepository;

    @Override
    protected User findUser(String email) {
        return userRepository.findByEmailForLogin(email);
    }

    @Override
    protected String notFoundMessage(String email) {
        return "No member with email: " + email;
    }
}
