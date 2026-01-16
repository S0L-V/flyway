package com.flyway.user.service;

import com.flyway.user.dto.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getUserProfile(String userId);
}
