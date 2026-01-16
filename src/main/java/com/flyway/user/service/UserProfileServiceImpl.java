package com.flyway.user.service;

import com.flyway.user.domain.User;
import com.flyway.user.domain.UserProfile;
import com.flyway.user.dto.UserProfileResponse;
import com.flyway.user.repository.UserProfileRepository;
import com.flyway.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getUserProfile(String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        User userInfo = userRepository.findById(userId);
        String createdAt = userInfo.getCreatedAt()
                .atZone(ZoneId.of("Asia/Seoul"))
                .toOffsetDateTime()
                .toString();


        if (profile == null) {
            throw new IllegalArgumentException("User not found. userId=" + userId);
        }

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

        System.out.println(response);

        return  response;

    }
}

/**
 * private String userId;
 * private String name; // 한글 이름
 * private String passportNo;
 * private String country; // 국적
 * private String gender; // M | F
 * private String firstName; // 영문 이름
 * private String lastName; // 영문 성
 */