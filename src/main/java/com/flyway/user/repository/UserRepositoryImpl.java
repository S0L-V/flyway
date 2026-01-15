package com.flyway.user.repository;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.domain.User;
import com.flyway.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public User findById(String userId) {
        return userMapper.findById(userId);
    }

    @Override
    public void save(User user) {
        userMapper.insertUser(user);
    }

    @Override
    public void updateEmail(String userId, String email) {
        userMapper.updateEmail(userId, email);
    }

    @Override
    public void updateStatus(String userId, AuthStatus status) {
        userMapper.updateStatus(userId, status.name());
    }
}
