package com.flyway.user.repository;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.user.domain.User;
import com.flyway.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public User findById(String userId) {
        return userMapper.findById(userId);
    }

    @Override
    public User findByEmailForLogin(String email) { return userMapper.findByEmailForLogin(email); }

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

    @Override
    public int markWithdrawn(String userId, LocalDateTime now) { return userMapper.markWithdrawn(userId, now); }
}
