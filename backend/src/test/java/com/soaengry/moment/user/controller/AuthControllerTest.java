package com.soaengry.moment.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaengry.moment.domain.email.repository.EmailVerificationRepository;
import com.soaengry.moment.domain.user.controller.RefreshRequest;
import com.soaengry.moment.domain.user.dto.request.LoginRequest;
import com.soaengry.moment.domain.user.dto.request.SignupRequest;
import com.soaengry.moment.domain.user.dto.request.VerifyEmailRequest;
import com.soaengry.moment.domain.user.dto.response.SignupResponse;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.user.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        emailVerificationRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/auth/signup - 회원가입 성공")
    void signup_Success() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.verificationCode").exists())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✅ 회원가입 API 테스트 통과");
    }

    @Test
    @DisplayName("POST /api/auth/signup - 유효성 검증 실패 (잘못된 비밀번호)")
    void signup_Fail_InvalidPassword() throws Exception {
        // given
        SignupRequest request = new SignupRequest(
                "test@example.com",
                "weak",
                "테스터"
        );

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        System.out.println("✅ 비밀번호 검증 실패 테스트 통과");
    }

    @Test
    @DisplayName("POST /api/auth/verify-email - 이메일 인증 성공")
    void verifyEmail_Success() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        VerifyEmailRequest request = new VerifyEmailRequest(
                "test@example.com",
                signupResponse.verificationCode()
        );

        // when & then
        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("이메일 인증이 완료되었습니다"));

        System.out.println("✅ 이메일 인증 API 테스트 통과");
    }

    @Test
    @DisplayName("POST /api/auth/login - 로그인 성공")
    void login_Success() throws Exception {
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
                "Chrome on Windows"
        );

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.expiresIn").exists());

        System.out.println("✅ 로그인 API 테스트 통과");
    }

    @Test
    @DisplayName("POST /api/auth/login - 잘못된 비밀번호로 로그인 실패")
    void login_Fail_WrongPassword() throws Exception {
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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        System.out.println("✅ 잘못된 비밀번호 로그인 테스트 통과");
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 토큰 갱신 성공")
    void refresh_Success() throws Exception {
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

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(loginResponseBody, TokenResponse.class);

        RefreshRequest refreshRequest = new RefreshRequest(tokenResponse.refreshToken());

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        System.out.println("✅ 토큰 갱신 API 테스트 통과");
    }

    @Test
    @DisplayName("POST /api/auth/logout - 로그아웃 성공")
    void logout_Success() throws Exception {
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

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponseBody = loginResult.getResponse().getContentAsString();
        TokenResponse tokenResponse = objectMapper.readValue(loginResponseBody, TokenResponse.class);

        RefreshRequest logoutRequest = new RefreshRequest(tokenResponse.refreshToken());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다"));

        System.out.println("✅ 로그아웃 API 테스트 통과");
    }
}

// Test DTO
record TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
}
