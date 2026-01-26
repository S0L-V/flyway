package com.flyway.user.repository;

import com.flyway.user.domain.UserProfile;
import com.flyway.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfileMapper userProfileMapper;

    @Override
    public void createProfile(UserProfile userProfile) {
        userProfileMapper.insertProfile(userProfile);
    }

    @Override
    public UserProfile findByUserId(String userId) {
        return userProfileMapper.findByUserId(userId);
    }

    @Override
    public void updateProfile(UserProfile profile) {
        userProfileMapper.updateProfile(profile);
    }

    @Override
    public int nullifyProfileIfWithdrawn(String userId, String maskedName) {
        return userProfileMapper.nullifyProfileIfWithdrawn(userId, maskedName);
    }
}
