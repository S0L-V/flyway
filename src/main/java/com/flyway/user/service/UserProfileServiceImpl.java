package com.flyway.user.service;

import com.flyway.user.domain.User;
import com.flyway.user.domain.UserProfile;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.repository.UserProfileRepository;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter CREATED_AT_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getUserProfile(String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            throw new IllegalArgumentException("User profile not found. userId=" + userId);
        }

        User userInfo = userRepository.findById(userId);
        if (userInfo == null) {
            throw new IllegalArgumentException("User not found. userId=" + userId);
        }

        String createdAt = userInfo.getCreatedAt()
                .atZone(KST)
                .toOffsetDateTime()
                .format(CREATED_AT_FMT);

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(userId)
                .name(profile.getName())
                .passportNo(profile.getPassportNo())
                .country(profile.getCountry())
                .gender(profile.getGender())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .status(userInfo.getStatus())
                .email(userInfo.getEmail())
                .createdAt(createdAt)
                .build();

        log.debug("getUserProfile userId={}, response={}", userId, response);
        return response;
    }
}
