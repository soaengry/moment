package com.soaengry.moment.domain.user.controller;

import com.soaengry.moment.domain.user.dto.request.CheckEmailRequest;
import com.soaengry.moment.domain.user.dto.request.CheckNicknameRequest;
import com.soaengry.moment.domain.user.dto.request.LoginRequest;
import com.soaengry.moment.domain.user.dto.request.RefreshRequest;
import com.soaengry.moment.domain.user.dto.request.ResendVerificationRequest;
import com.soaengry.moment.domain.user.dto.request.SignupRequest;
import com.soaengry.moment.domain.user.dto.response.CheckEmailResponse;
import com.soaengry.moment.domain.user.dto.response.CheckEmailVerifiedResponse;
import com.soaengry.moment.domain.user.dto.response.CheckNicknameResponse;
import com.soaengry.moment.domain.user.dto.response.LogoutResponse;
import com.soaengry.moment.domain.user.dto.response.ResendVerificationResponse;
import com.soaengry.moment.domain.user.dto.response.SignupResponse;
import com.soaengry.moment.domain.user.dto.response.VerificationStatusResponse;
import com.soaengry.moment.domain.user.dto.response.TokenResponse;
import com.soaengry.moment.domain.user.exception.UserErrorCode;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.user.repository.OAuth2CodeRepository;
import com.soaengry.moment.domain.user.service.AuthService;
import com.soaengry.moment.domain.user.service.UserService;
import com.soaengry.moment.global.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final OAuth2CodeRepository oAuth2CodeRepository;
    private final JwtProperties jwtProperties;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

//    /**
//     * 이메일 인증 (POST - 코드 입력 방식)
//     */
//    @PostMapping("/verify-email")
//    public ResponseEntity<VerifyEmailResponse> verifyEmail(
//            @Valid @RequestBody VerifyEmailRequest request
//    ) {
//        authService.verifyEmail(request);
//        return ResponseEntity.ok(new VerifyEmailResponse("이메일 인증이 완료되었습니다"));
//    }

    /**
     * 이메일 인증 (GET - 링크 클릭 방식)
     * 브라우저에서 직접 열리므로 HTML 페이지 반환
     */
    @GetMapping(value = "/verify-email", produces = "text/html;charset=UTF-8")
    public ResponseEntity<String> verifyEmailByToken(
            @RequestParam("token") String token
    ) {
        String resultMessage;
        String resultType;
        try {
            authService.verifyEmailByToken(token);
            resultMessage = "이메일 인증이 완료되었습니다";
            resultType = "success";
        } catch (Exception e) {
            resultMessage = e.getMessage();
            resultType = "error";
        }

        String html = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>이메일 인증</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #FAFFF4; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
                        .card { background: white; border-radius: 16px; padding: 48px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); text-align: center; max-width: 400px; }
                        .icon { width: 56px; height: 56px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; font-size: 24px; }
                        .icon.success { background: #E8F5E9; color: #4CAF50; }
                        .icon.error { background: #FFEBEE; color: #F44336; }
                        .message { font-size: 16px; color: #333; margin-bottom: 8px; font-weight: 600; }
                        .sub { font-size: 14px; color: #888; }
                    </style>
                </head>
                <body>
                    <div class="card">
                        <div class="icon %s">%s</div>
                        <p class="message">%s</p>
                        <p class="sub">%s</p>
                    </div>
                    <script>
                        if ("%s" === "success") {
                            setTimeout(function() { window.close(); }, 2000);
                        }
                    </script>
                </body>
                </html>
                """.formatted(
                resultType,
                resultType.equals("success") ? "&#10003;" : "&#10007;",
                resultMessage,
                resultType.equals("success") ? "잠시 후 창이 닫힙니다..." : "다시 시도해주세요",
                resultType
        );

        return ResponseEntity.ok(html);
    }

    /**
     * OAuth2 로그인 후 일회용 코드로 토큰 교환
     */
    @GetMapping("/oauth2/token")
    public ResponseEntity<TokenResponse> exchangeOAuth2Token(@RequestParam("code") String code) {
        String[] tokens = oAuth2CodeRepository.getAndDelete(code);
        if (tokens == null || tokens.length < 2) {
            throw new UserException(UserErrorCode.AUTH_INVALID_CREDENTIALS, "유효하지 않거나 만료된 OAuth2 코드입니다");
        }
        return ResponseEntity.ok(TokenResponse.of(tokens[0], tokens[1], jwtProperties.accessTokenExpiration() / 1000));
    }

    /**
     * 회원가입 전 이메일 인증 발송
     */
    @PostMapping("/send-signup-verification")
    public ResponseEntity<ResendVerificationResponse> sendSignupVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        authService.sendSignupVerificationEmail(request.email());
        return ResponseEntity.ok(new ResendVerificationResponse("인증 메일을 발송했습니다"));
    }

    /**
     * 이메일 인증 상태 조회 (회원가입 전용)
     */
    @GetMapping("/verification-status")
    public ResponseEntity<VerificationStatusResponse> getVerificationStatus(
            @RequestParam("email") String email
    ) {
        boolean verified = authService.isEmailVerifiedForSignup(email);
        return ResponseEntity.ok(new VerificationStatusResponse(verified));
    }

    /**
     * 이메일 인증 코드 재발송
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ResendVerificationResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        authService.resendVerificationEmail(request);
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
     * 이메일 인증 완료 여부 조회
     */
    @GetMapping("/check-email-verified")
    public ResponseEntity<CheckEmailVerifiedResponse> checkEmailVerified(@RequestParam String email) {
        boolean verified = authService.isEmailVerifiedForSignup(email);
        return ResponseEntity.ok(new CheckEmailVerifiedResponse(verified));
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