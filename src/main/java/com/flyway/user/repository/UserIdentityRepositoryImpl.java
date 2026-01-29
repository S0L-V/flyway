package com.flyway.user.repository;

import com.flyway.auth.domain.AuthProvider;
import com.flyway.user.domain.UserIdentity;
import com.flyway.user.mapper.UserIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserIdentityRepositoryImpl implements UserIdentityRepository {

    private final UserIdentityMapper userIdentityMapper;

    @Override
    public void save(UserIdentity identity) {
        userIdentityMapper.insertIdentity(identity);
    }

    @Override
    public UserIdentity findByUserId(String userId) {
        return userIdentityMapper.findByUserId(userId);
    }

    @Override
    public UserIdentity findByProviderUserId(AuthProvider provider, String providerUserId) {
        return userIdentityMapper.findByProviderUserId(provider, providerUserId);
    }

    @Override
    public boolean existsEmailIdentity(String email) {
        return userIdentityMapper.existsEmailIdentity(email);
    }

    @Override
    public int anonymizeProviderUserIdIfWithdrawn(String userId, AuthProvider provider, String anonymizedProviderUserId) {
        return userIdentityMapper.anonymizeProviderUserIdIfWithdrawn(userId, provider.toString(), anonymizedProviderUserId);
    }
}
