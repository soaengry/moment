package com.soaengry.moment.global.security.oauth2;

import com.soaengry.moment.global.util.NicknameGenerator;
import com.soaengry.moment.user.entity.User;
import com.soaengry.moment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        log.info("OAuth2 로그인 시도 - Provider: {}, Email: {}", registrationId, userInfo.getEmail());
        
        User user = userRepository.findByEmail(userInfo.getEmail())
            .map(existingUser -> updateExistingUser(existingUser, userInfo))
            .orElseGet(() -> registerNewUser(userInfo, registrationId));
        
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        // 이미 탈퇴한 사용자인 경우 복구
        if (user.isDeleted()) {
            user.restore();
            log.info("탈퇴한 사용자 복구 - ID: {}", user.getId());
        }
        
        return user;
    }

    /**
     * 신규 사용자 등록
     */
    private User registerNewUser(OAuth2UserInfo userInfo, String provider) {
        // 중복되지 않는 랜덤 닉네임 생성
        String nickname = generateUniqueNickname();
        
        User newUser = User.builder()
            .email(userInfo.getEmail())
            .nickname(nickname)
            .profileImageUrl(userInfo.getProfileImageUrl())
            .role(User.Role.USER)
            .authProvider(User.AuthProvider.valueOf(provider.toUpperCase()))
            .providerId(userInfo.getProviderId())
            .isEmailVerified(true)  // 소셜 로그인은 이메일 인증 완료로 간주
            .build();
        
        userRepository.save(newUser);
        
        log.info("신규 소셜 로그인 사용자 등록 - Provider: {}, Email: {}, Nickname: {}", 
            provider, userInfo.getEmail(), nickname);
        
        return newUser;
    }

    /**
     * 중복되지 않는 랜덤 닉네임 생성
     */
    private String generateUniqueNickname() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            String nickname = NicknameGenerator.generate();
            if (!userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        
        // 10번 시도해도 중복이면 숫자 추가
        String baseNickname = NicknameGenerator.generate();
        int suffix = 1;
        while (userRepository.existsByNickname(baseNickname + suffix)) {
            suffix++;
        }
        return baseNickname + suffix;
    }
}
