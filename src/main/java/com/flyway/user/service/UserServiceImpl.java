package com.flyway.user.service;

import com.flyway.auth.domain.AuthStatus;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.domain.User;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	@Transactional
	public AuthStatus blockUser(String userId) {
		String targetUserId = requireUserId(userId);
		User user = userRepository.findById(targetUserId);
		if (user == null) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
		}

		userRepository.updateStatus(targetUserId, AuthStatus.BLOCKED);
		log.info("User blocked: userId={}", targetUserId);
		return AuthStatus.BLOCKED;
	}

	@Override
	@Transactional
	public AuthStatus unblockUser(String userId) {
		String targetUserId = requireUserId(userId);
		User user = userRepository.findById(targetUserId);
		if (user == null) {
			throw new BusinessException(ErrorCode.USER_NOT_FOUND);
		}

		userRepository.updateStatus(targetUserId, AuthStatus.ACTIVE);
		log.info("User unblocked: userId={}", targetUserId);
		return AuthStatus.ACTIVE;
	}

	private String requireUserId(String userId) {
		if (userId == null || userId.isBlank()) {
			throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
		}
		return userId;
	}
}
