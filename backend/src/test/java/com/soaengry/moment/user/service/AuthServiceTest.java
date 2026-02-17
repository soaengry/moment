package com.soaengry.moment.user.service;

import com.soaengry.moment.global.exception.CustomException;
import com.soaengry.moment.global.exception.ErrorCode;
import com.soaengry.moment.global.security.JwtProvider;
import com.soaengry.moment.user.dto.request.LoginRequest;
import com.soaengry.moment.user.dto.request.SignupRequest;
import com.soaengry.moment.user.dto.request.VerifyEmailRequest;
import com.soaengry.moment.user.dto.response.SignupResponse;
import com.soaengry.moment.user.dto.response.TokenResponse;
import com.soaengry.moment.user.entity.User;
import com.soaengry.moment.user.repository.EmailVerificationRepository;
import com.soaengry.moment.user.repository.RefreshTokenRepository;
import com.soaengry.moment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 정리
        userRepository.deleteAll();
        emailVerificationRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_Success() {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.verificationCode()).hasSize(6);

        // DB 확인
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(user.getNickname()).isEqualTo("테스터");
        assertThat(user.getIsEmailVerified()).isFalse();
        assertThat(passwordEncoder.matches("Test1234!@", user.getPassword())).isTrue();

        System.out.println("✅ 회원가입 성공 테스트 통과");
        System.out.println("   - 사용자 ID: " + response.userId());
        System.out.println("   - 인증 코드: " + response.verificationCode());
    }

    @Test
    @DisplayName("이메일 중복 시 회원가입 실패")
    void signup_Fail_DuplicateEmail() {
        // given
        SignupRequest request1 = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터1"
        );
        authService.signup(request1);

        SignupRequest request2 = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터2"
        );

        // when & then
        assertThatThrownBy(() -> authService.signup(request2))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.DUPLICATE_001.getMessage());

        System.out.println("✅ 이메일 중복 테스트 통과");
    }

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        VerifyEmailRequest verifyRequest = new VerifyEmailRequest(
                "test@example.com",
                signupResponse.verificationCode()
        );

        // when
        authService.verifyEmail(verifyRequest);

        // then
        User user = userRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(user.getIsEmailVerified()).isTrue();

        System.out.println("✅ 이메일 인증 성공 테스트 통과");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        // 1. 회원가입
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        // 2. 이메일 인증
        VerifyEmailRequest verifyRequest = new VerifyEmailRequest(
                "test@example.com",
                signupResponse.verificationCode()
        );
        authService.verifyEmail(verifyRequest);

        // 3. 로그인
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "Test1234!@",
                "device-123",
                "Chrome on Windows"
        );

        // when
        TokenResponse tokenResponse = authService.login(loginRequest);

        // then
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.accessToken()).isNotEmpty();
        assertThat(tokenResponse.refreshToken()).isNotEmpty();
        assertThat(tokenResponse.expiresIn()).isPositive();

        // JWT 검증
        assertThat(jwtProvider.validateToken(tokenResponse.accessToken())).isTrue();

        // Redis에 RefreshToken 저장 확인
        Long userId = jwtProvider.getUserIdFromToken(tokenResponse.refreshToken());
        String deviceId = jwtProvider.getDeviceIdFromToken(tokenResponse.refreshToken());
        assertThat(refreshTokenRepository.existsByToken(userId, deviceId, tokenResponse.refreshToken())).isTrue();

        System.out.println("✅ 로그인 성공 테스트 통과");
        System.out.println("   - Access Token: " + tokenResponse.accessToken().substring(0, 20) + "...");
        System.out.println("   - Refresh Token: " + tokenResponse.refreshToken().substring(0, 20) + "...");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패")
    void login_Fail_WrongPassword() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        authService.signup(signupRequest);

        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "WrongPassword123!",
                null,
                null
        );

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.AUTH_001.getMessage());

        System.out.println("✅ 잘못된 비밀번호 테스트 통과");
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refresh_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        VerifyEmailRequest verifyRequest = new VerifyEmailRequest(
                "test@example.com",
                signupResponse.verificationCode()
        );
        authService.verifyEmail(verifyRequest);

        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "Test1234!@",
                "device-123",
                "Chrome"
        );
        TokenResponse loginResponse = authService.login(loginRequest);

        // when
        TokenResponse refreshResponse = authService.refresh(loginResponse.refreshToken());

        // then
        assertThat(refreshResponse).isNotNull();
        assertThat(refreshResponse.accessToken()).isNotEmpty();
        assertThat(refreshResponse.refreshToken()).isNotEmpty();

        // 새 토큰은 기존 토큰과 달라야 함
        assertThat(refreshResponse.accessToken()).isNotEqualTo(loginResponse.accessToken());
        assertThat(refreshResponse.refreshToken()).isNotEqualTo(loginResponse.refreshToken());

        // 기존 토큰은 삭제되어야 함 (해시 기반 검증)
        Long userId = jwtProvider.getUserIdFromToken(loginResponse.refreshToken());
        String deviceId = jwtProvider.getDeviceIdFromToken(loginResponse.refreshToken());
        assertThat(refreshTokenRepository.existsByToken(userId, deviceId, loginResponse.refreshToken()))
                .isFalse();

        // 새 토큰은 존재해야 함 (해시 기반 검증)
        Long newUserId = jwtProvider.getUserIdFromToken(refreshResponse.refreshToken());
        String newDeviceId = jwtProvider.getDeviceIdFromToken(refreshResponse.refreshToken());
        assertThat(refreshTokenRepository.existsByToken(newUserId, newDeviceId, refreshResponse.refreshToken()))
                .isTrue();

        System.out.println("✅ 토큰 갱신 성공 테스트 통과");
        System.out.println("   - 기존 토큰은 삭제됨 (Rotation)");
        System.out.println("   - 새 토큰 발급 완료");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        VerifyEmailRequest verifyRequest = new VerifyEmailRequest(
                "test@example.com",
                signupResponse.verificationCode()
        );
        authService.verifyEmail(verifyRequest);

        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "Test1234!@",
                null,
                null
        );
        TokenResponse tokenResponse = authService.login(loginRequest);

        // when
        authService.logout(tokenResponse.refreshToken());

        // then
        Long userId = jwtProvider.getUserIdFromToken(tokenResponse.refreshToken());
        String deviceId = jwtProvider.getDeviceIdFromToken(tokenResponse.refreshToken());
        assertThat(refreshTokenRepository.existsByToken(userId, deviceId, tokenResponse.refreshToken())).isFalse();

        System.out.println("✅ 로그아웃 성공 테스트 통과");
        System.out.println("   - RefreshToken Redis에서 삭제 확인");
    }
}
