package com.soaengry.moment.domain.user.service;

import com.soaengry.moment.domain.user.dto.response.UserResponse;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.exception.UserErrorCode;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.user.repository.RefreshTokenRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserResponse updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = userRepository.findByIdWithOptimisticLock(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 닉네임 중복 체크
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) {
                throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
            }
        }

        // 프로필 업데이트 (이미지 URL이 null이면 이미지 삭제)
        user.updateProfile(nickname, profileImageUrl);

        log.info("프로필 수정 완료 - 사용자 ID: {}, 이미지 URL: {}", userId, profileImageUrl);

        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 소셜 로그인 사용자 체크
        if (user.isSocialLogin()) {
            throw new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // 비밀번호 변경 및 토큰 버전 증가
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);

        // 모든 RefreshToken 삭제 (재로그인 필요)
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("AUDIT | event=PASSWORD_CHANGED userId={}", userId);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // Soft Delete
        user.softDelete();

        // 모든 RefreshToken 삭제
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("AUDIT | event=ACCOUNT_DELETED userId={}", userId);
    }

    /**
     * 회원 복구
     */
    @Transactional
    public void restoreAccount(String email, String password) {
        User user = userRepository.findDeletedUserByEmailAfter(
                email,
                java.time.LocalDateTime.now().minusDays(30)
        ).orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, "복구 가능한 계정이 없습니다"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS);
        }

        // 계정 복구
        user.restore();

        log.info("AUDIT | event=ACCOUNT_RESTORED userId={}", user.getId());
    }

    /**
     * 이메일 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean checkNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}