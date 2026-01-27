package com.flyway.user.service;

import com.flyway.auth.domain.AuthStatus;

public interface UserService {

	AuthStatus blockUser(String userId);

	AuthStatus unblockUser(String userId);
}
