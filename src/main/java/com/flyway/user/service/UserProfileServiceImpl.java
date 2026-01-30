package com.flyway.user.service;

import com.flyway.user.domain.User;
import com.flyway.user.domain.UserProfile;
import com.flyway.template.exception.BusinessException;
import com.flyway.template.exception.ErrorCode;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.dto.UserProfileUpdateRequest;
import com.flyway.user.repository.UserProfileRepository;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return toResponse(userInfo, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String userId, UserProfileUpdateRequest request) {
        String targetUserId = requireUserId(userId);
        if (request == null) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }

        User userInfo = userRepository.findById(targetUserId);
        if (userInfo == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserProfile existing = userProfileRepository.findByUserId(targetUserId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserProfile update = UserProfile.builder()
                .userId(targetUserId)
                .krFirstName(normalize(request.getKrFirstName()))
                .krLastName(normalize(request.getKrLastName()))
                .birth(request.getBirth())
                .phoneNumber(normalize(request.getPhoneNumber()))
                .passportNo(normalize(request.getPassportNo()))
                .passportExpiryDate(request.getPassportExpiryDate())
                .passportIssueCountry(normalize(request.getPassportIssueCountry()))
                .country(normalize(request.getCountry()))
                .gender(normalizeGender(request.getGender()))
                .firstName(normalize(request.getFirstName()))
                .lastName(normalize(request.getLastName()))
                .build();

        if (!hasAnyUpdate(update)) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }

        userProfileRepository.updateProfile(update);

        applyUpdates(existing, update);

        return toResponse(userInfo, existing);
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        return userId;
    }

    private boolean hasAnyUpdate(UserProfile profile) {
        return profile.getKrFirstName() != null
                || profile.getKrLastName() != null
                || profile.getBirth() != null
                || profile.getPhoneNumber() != null
                || profile.getPassportNo() != null
                || profile.getPassportExpiryDate() != null
                || profile.getPassportIssueCountry() != null
                || profile.getCountry() != null
                || profile.getGender() != null
                || profile.getFirstName() != null
                || profile.getLastName() != null;
    }

    private void applyUpdates(UserProfile target, UserProfile update) {
        if (update.getKrFirstName() != null) target.setKrFirstName(update.getKrFirstName());
        if (update.getKrLastName() != null) target.setKrLastName(update.getKrLastName());
        if (update.getBirth() != null) target.setBirth(update.getBirth());
        if (update.getPhoneNumber() != null) target.setPhoneNumber(update.getPhoneNumber());
        if (update.getPassportNo() != null) target.setPassportNo(update.getPassportNo());
        if (update.getPassportExpiryDate() != null) target.setPassportExpiryDate(update.getPassportExpiryDate());
        if (update.getPassportIssueCountry() != null) target.setPassportIssueCountry(update.getPassportIssueCountry());
        if (update.getCountry() != null) target.setCountry(update.getCountry());
        if (update.getGender() != null) target.setGender(update.getGender());
        if (update.getFirstName() != null) target.setFirstName(update.getFirstName());
        if (update.getLastName() != null) target.setLastName(update.getLastName());
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeGender(String gender) {
        String normalized = normalize(gender);
        if (normalized == null) return null;
        String upper = normalized.toUpperCase();
        if (!"M".equals(upper) && !"F".equals(upper)) {
            throw new BusinessException(ErrorCode.USER_INVALID_INPUT);
        }
        return upper;
    }

    private UserProfileResponse toResponse(User userInfo, UserProfile profile) {
        String createdAt = userInfo.getCreatedAt()
                .atZone(KST)
                .toOffsetDateTime()
                .format(CREATED_AT_FMT);

        return UserProfileResponse.builder()
                .userId(userInfo.getUserId())
                .name(profile.getName())
                .krFirstName(profile.getKrFirstName())
                .krLastName(profile.getKrLastName())
                .birth(profile.getBirth())
                .phoneNumber(profile.getPhoneNumber())
                .passportExpiryDate(profile.getPassportExpiryDate())
                .passportIssueCountry(profile.getPassportIssueCountry())
                .passportNo(profile.getPassportNo())
                .country(profile.getCountry())
                .gender(profile.getGender())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .status(userInfo.getStatus())
                .email(profile.getEmail() != null ? profile.getEmail() : userInfo.getEmail())
                .createdAt(createdAt)
                .build();
    }
}
