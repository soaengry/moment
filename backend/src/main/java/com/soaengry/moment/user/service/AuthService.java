package com.soaengry.moment.user.service;

import com.soaengry.moment.global.exception.BusinessException;
import com.soaengry.moment.global.exception.ErrorCode;
import com.soaengry.moment.global.security.JwtProvider;
import com.soaengry.moment.global.util.CodeGenerator;
import com.soaengry.moment.user.dto.LoginRequest;
import com.soaengry.moment.user.dto.SignupRequest;
import com.soaengry.moment.user.dto.TokenResponse;
import com.soaengry.moment.user.dto.VerifyEmailRequest;
import com.soaengry.moment.user.entity.EmailVerification;
import com.soaengry.moment.user.entity.User;
import com.soaengry.moment.user.repository.EmailVerificationRepository;
import com.soaengry.moment.user.repository.RefreshTokenRepository;
import com.soaengry.moment.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${app.max-devices-per-user}")
    private int maxDevicesPerUser;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    /**
     * 회원가입
     */
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_001);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_002);
        }

        // 사용자 생성
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = request.toEntity(encodedPassword);
        user = userRepository.save(user);

        // 이메일 인증 코드 생성 및 저장
        String verificationCode = CodeGenerator.generateAlphanumeric(6);
        EmailVerification verification = EmailVerification.builder()
                .email(user.getEmail())
                .verificationCode(verificationCode)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
        emailVerificationRepository.save(verification);

        log.info("회원가입 완료 - 사용자 ID: {}, 이메일: {}, 인증 코드: {}",
                user.getId(), user.getEmail(), verificationCode);

        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                verificationCode,
                "회원가입이 완료되었습니다. 이메일 인증을 진행해주세요."
        );
    }

    /**
     * 이메일 인증
     */
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        // 인증 정보 조회
        EmailVerification verification = emailVerificationRepository
                .findLatestByEmailAndCode(request.email(), request.verificationCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_007));

        // 잠금 상태 확인
        if (verification.isLockedNow()) {
            throw new BusinessException(ErrorCode.AUTH_006);
        }

        // 만료 확인
        if (verification.isExpired()) {
            throw new BusinessException(ErrorCode.AUTH_002, "인증 코드가 만료되었습니다");
        }

        // 인증 처리
        verification.verify();

        // 사용자 이메일 인증 상태 업데이트
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));
        user.verifyEmail();

        log.info("이메일 인증 완료 - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));

        // 탈퇴한 사용자 체크
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.AUTH_005);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }

        // Device ID 생성
        String deviceId = request.deviceId() != null ?
                request.deviceId() : UUID.randomUUID().toString();

        // 다중 디바이스 관리
        manageDeviceLimit(user.getId());

        // Token 생성
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(
                user.getId(),
                deviceId,
                user.getTokenVersion()
        );

        // RefreshToken Redis 저장 (해시값만)
        refreshTokenRepository.save(user.getId(), deviceId, refreshToken);

        log.info("로그인 완료 - 사용자 ID: {}, 디바이스: {}", user.getId(), deviceId);

        return TokenResponse.of(accessToken, refreshToken, accessTokenExpiration);
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        // JWT 파싱
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        String deviceId = jwtProvider.getDeviceIdFromToken(refreshToken);
        Integer tokenVersion = jwtProvider.getTokenVersionFromToken(refreshToken);

        // Redis에서 토큰 검증 (해시 기반)
        boolean exists = refreshTokenRepository.existsByToken(userId, deviceId, refreshToken);
        if (!exists) {
            throw new BusinessException(ErrorCode.AUTH_002, "Refresh Token이 유효하지 않습니다");
        }

        // 사용자 조회 및 token version 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));

        if (!user.getTokenVersion().equals(tokenVersion)) {
            // 보안 위협 감지 - 모든 토큰 무효화
            refreshTokenRepository.deleteAllByUserId(userId);
            throw new BusinessException(ErrorCode.AUTH_008);
        }

        // Refresh Token Rotation (기존 토큰 삭제)
        refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);

        // 새 토큰 생성
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId, deviceId, user.getTokenVersion());

        // 새 RefreshToken 해시 저장
        refreshTokenRepository.save(userId, deviceId, newRefreshToken);

        log.info("토큰 갱신 완료 - 사용자 ID: {}", userId);

        return TokenResponse.of(newAccessToken, newRefreshToken, accessTokenExpiration);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String refreshToken) {
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        String deviceId = jwtProvider.getDeviceIdFromToken(refreshToken);

        refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
        log.info("로그아웃 완료 - 사용자 ID: {}, 디바이스: {}", userId, deviceId);
    }

    /**
     * 모든 디바이스에서 로그아웃
     */
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);

        // Token Version 증가
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_001));
        user.incrementTokenVersion();

        log.info("모든 디바이스 로그아웃 완료 - 사용자 ID: {}", userId);
    }

    /**
     * 디바이스 제한 관리
     */
    private void manageDeviceLimit(Long userId) {
        long activeTokenCount = refreshTokenRepository.countActiveTokensByUserId(userId);

        if (activeTokenCount >= maxDevicesPerUser) {
            refreshTokenRepository.deleteOldestToken(userId);
            log.info("디바이스 제한 초과 - 가장 오래된 토큰 삭제");
        }
    }
}

// 회원가입 응답 DTO
record SignupResponse(Long userId, String email, String verificationCode, String message) {
}
