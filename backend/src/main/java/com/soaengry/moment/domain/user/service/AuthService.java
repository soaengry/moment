package com.soaengry.moment.domain.user.service;

import com.soaengry.moment.domain.email.entity.EmailVerification;
import com.soaengry.moment.domain.email.repository.EmailVerificationRepository;
import com.soaengry.moment.domain.email.service.EmailService;
import com.soaengry.moment.domain.user.dto.request.LoginRequest;
import com.soaengry.moment.domain.user.dto.request.ResendVerificationRequest;
import com.soaengry.moment.domain.user.dto.request.SignupRequest;
import com.soaengry.moment.domain.user.dto.response.SignupResponse;
import com.soaengry.moment.domain.user.dto.response.TokenResponse;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.exception.UserErrorCode;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.user.repository.RefreshTokenRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.global.security.JwtProvider;
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
    private final EmailService emailService;

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
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.nickname())) {
            throw new UserException(UserErrorCode.DUPLICATE_NICKNAME);
        }

        // 사용자 생성
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = request.toEntity(encodedPassword);
        user = userRepository.save(user);

        // 이메일 인증 토큰 생성 (UUID 사용)
        String verificationToken = generateVerificationToken(user.getEmail());

        // 이메일 발송 (토큰 링크)
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        log.info("회원가입 완료 - 사용자 ID: {}, 이메일: {}",
                user.getId(), user.getEmail());

        return new SignupResponse(
                user.getId(),
                user.getEmail(),
                null,  // 토큰은 응답에 포함하지 않음 (이메일로만 전송)
                "회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요."
        );
    }

    /**
     * 이메일 재발송 (토큰 링크)
     */
    public void resendVerificationEmail(ResendVerificationRequest request) {

        // 이메일 인증 토큰 생성 (UUID 사용)
        String verificationToken = generateVerificationToken(request.email());

        // 이메일 발송 (토큰 링크)
        emailService.sendVerificationEmail(request.email(), verificationToken);
    }

    /**
     * 이메일 인증 (코드 입력 방식)
     */
//    @Transactional
//    public void verifyEmail(VerifyEmailRequest request) {
//        // 인증 정보 조회
//        EmailVerification verification = emailVerificationRepository
//                .findLatestByEmailAndCode(request.email(), request.verificationCode())
//                .orElseThrow(() -> new UserException(UserErrorCode.AUTH_VERIFICATION_CODE_MISMATCH));
//
//        // 잠금 상태 확인
//        if (verification.isLockedNow()) {
//            throw new UserException(UserErrorCode.AUTH_VERIFICATION_ATTEMPTS_EXCEEDED);
//        }
//
//        // 만료 확인
//        if (verification.isExpired()) {
//            throw new UserException(UserErrorCode.AUTH_TOKEN_EXPIRED, "인증 코드가 만료되었습니다");
//        }
//
//        // 인증 처리
//        verification.verify();
//
//        // 사용자 이메일 인증 상태 업데이트
//        User user = userRepository.findByEmail(request.email())
//                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
//        user.verifyEmail();
//
//        log.info("이메일 인증 완료 - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());
//    }

    /**
     * 이메일 인증 (토큰 링크 클릭 방식)
     */
    @Transactional
    public void verifyEmailByToken(String token) {
        // 토큰으로 인증 정보 조회
        EmailVerification verification = emailVerificationRepository
                .findByVerificationCode(token)
                .orElseThrow(() -> new UserException(UserErrorCode.AUTH_VERIFICATION_CODE_MISMATCH, "유효하지 않은 인증 링크입니다"));

        // 이미 인증된 경우
        if (verification.getIsVerified()) {
            throw new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS, "이미 인증이 완료되었습니다");
        }

        // 만료 확인
        if (verification.isExpired()) {
            throw new UserException(UserErrorCode.AUTH_TOKEN_EXPIRED, "인증 링크가 만료되었습니다");
        }

        // 인증 처리
        verification.verify();

        // 사용자 이메일 인증 상태 업데이트
        User user = userRepository.findByEmail(verification.getEmail())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        user.verifyEmail();

        log.info("이메일 인증 완료 (토큰 방식) - 사용자 ID: {}, 이메일: {}", user.getId(), user.getEmail());
    }

    /**
     * 로그인
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS));

        // 탈퇴한 사용자 체크
        if (user.isDeleted()) {
            throw new UserException(UserErrorCode.AUTH_ACCOUNT_WITHDRAWN);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS);
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

        // Redis에서 토큰 검증
        if (!refreshTokenRepository.existsByToken(userId, deviceId, refreshToken)) {
            throw new UserException(UserErrorCode.AUTH_TOKEN_EXPIRED);
        }

        // 사용자 조회 및 token version 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (!user.getTokenVersion().equals(tokenVersion)) {
            // 보안 위협 감지 - 모든 토큰 무효화
            refreshTokenRepository.deleteAllByUserId(userId);
            throw new UserException(UserErrorCode.AUTH_SECURITY_THREAT_DETECTED);
        }

        // Refresh Token Rotation (기존 토큰 삭제)
        refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);

        // 새 토큰 생성
        String newAccessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(
                userId, deviceId, user.getTokenVersion()
        );

        // 새 RefreshToken 저장
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
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
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

    /**
     * 이메일 인증 토큰 발행
     */
    private String generateVerificationToken(String email) {
        String verificationToken = UUID.randomUUID().toString();

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .verificationCode(verificationToken)  // 긴 토큰 저장
                .expiresAt(LocalDateTime.now().plusHours(24))  // 24시간 유효
                .build();
        emailVerificationRepository.save(verification);

        return verificationToken;
    }
}