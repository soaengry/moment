package com.soaengry.moment.user.service;

import com.soaengry.moment.global.exception.BusinessException;
import com.soaengry.moment.global.exception.ErrorCode;
import com.soaengry.moment.user.dto.response.UserResponse;
import com.soaengry.moment.user.entity.User;
import com.soaengry.moment.user.repository.RefreshTokenRepository;
import com.soaengry.moment.user.repository.UserRepository;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));

        return UserResponse.from(user);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserResponse updateProfile(Long userId, String nickname, String profileImageUrl) {
        User user = userRepository.findByIdWithOptimisticLock(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));

        // 닉네임 중복 체크
        if (nickname != null && !nickname.equals(user.getNickname())) {
            if (userRepository.existsByNickname(nickname)) {
                throw new BusinessException(ErrorCode.DUPLICATE_002);
            }
        }

        user.updateProfile(nickname, profileImageUrl);

        log.info("프로필 수정 완료 - 사용자 ID: {}", userId);

        return UserResponse.from(user);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));

        // 소셜 로그인 사용자 체크
        if (user.isSocialLogin()) {
            throw new BusinessException(ErrorCode.AUTH_001, "소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }

        // 비밀번호 변경 및 토큰 버전 증가
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedNewPassword);

        // 모든 RefreshToken 삭제 (재로그인 필요)
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("비밀번호 변경 완료 - 사용자 ID: {}", userId);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));

        // Soft Delete
        user.softDelete();

        // 모든 RefreshToken 삭제
        refreshTokenRepository.deleteAllByUserId(userId);

        log.info("회원 탈퇴 완료 - 사용자 ID: {}", userId);
    }

    /**
     * 회원 복구
     */
    @Transactional
    public void restoreAccount(String email, String password) {
        User user = userRepository.findDeletedUserByEmailAfter(
                email,
                java.time.LocalDateTime.now().minusDays(30)
        ).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001, "복구 가능한 계정이 없습니다"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }

        // 계정 복구
        user.restore();

        log.info("계정 복구 완료 - 사용자 ID: {}", user.getId());
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
