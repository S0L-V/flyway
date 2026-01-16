package com.flyway.security.service;

import com.flyway.user.domain.User;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("userIdUserDetailsService")
@RequiredArgsConstructor
public class UserIdUserDetailsService extends AbstractUserDetailsService {

    private final UserRepository userRepository;

    @Override
    protected User findUser(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    protected String notFoundMessage(String userId) {
        return "No member with id: " + userId;
    }
}
