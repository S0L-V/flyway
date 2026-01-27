package com.flyway.user.service;

import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.dto.UserProfileUpdateRequest;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String userId);

    UserProfileResponse updateProfile(String userId, UserProfileUpdateRequest request);
}
