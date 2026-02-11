package com.soaengry.moment.user.controller;

import com.soaengry.moment.user.dto.request.LoginRequest;
import com.soaengry.moment.user.dto.request.SignupRequest;
import com.soaengry.moment.user.dto.request.VerifyEmailRequest;
import com.soaengry.moment.user.dto.response.SignupResponse;
import com.soaengry.moment.user.dto.response.TokenResponse;
import com.soaengry.moment.user.service.AuthService;
import com.soaengry.moment.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 인증
     */
    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(new VerifyEmailResponse("이메일 인증이 완료되었습니다"));
    }

    /**
     * 이메일 인증 코드 재발송
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ResendVerificationResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        // TODO: EmailService 구현 후 추가
        return ResponseEntity.ok(new ResendVerificationResponse("인증 코드를 재발송했습니다"));
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        TokenResponse response = authService.login(request);
        log.info("로그인 성공 - IP: {}", getClientIp(httpRequest));
        return ResponseEntity.ok(response);
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(new LogoutResponse("로그아웃되었습니다"));
    }

    /**
     * 이메일 중복 체크
     */
    @PostMapping("/check-email")
    public ResponseEntity<CheckEmailResponse> checkEmail(
            @Valid @RequestBody CheckEmailRequest request
    ) {
        boolean exists = userService.checkEmailExists(request.email());
        return ResponseEntity.ok(new CheckEmailResponse(exists));
    }

    /**
     * 닉네임 중복 체크
     */
    @PostMapping("/check-nickname")
    public ResponseEntity<CheckNicknameResponse> checkNickname(
            @Valid @RequestBody CheckNicknameRequest request
    ) {
        boolean exists = userService.checkNicknameExists(request.nickname());
        return ResponseEntity.ok(new CheckNicknameResponse(exists));
    }

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

// Response DTOs
record VerifyEmailResponse(String message) {
}

record ResendVerificationResponse(String message) {
}

record LogoutResponse(String message) {
}

record CheckEmailResponse(boolean exists) {
}

record CheckNicknameResponse(boolean exists) {
}

// Request DTOs
record RefreshRequest(@jakarta.validation.constraints.NotBlank String refreshToken) {
}

record ResendVerificationRequest(
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        String email
) {
}

record CheckEmailRequest(
        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        String email
) {
}

record CheckNicknameRequest(
        @jakarta.validation.constraints.NotBlank
        String nickname
) {
}
