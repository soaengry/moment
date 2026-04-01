package com.soaengry.moment.global.security.oauth2;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.OAuth2CodeRepository;
import com.soaengry.moment.domain.user.repository.RefreshTokenRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.global.security.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final OAuth2CodeRepository oAuth2CodeRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getUserId();

        // User 정보 조회 (tokenVersion 필요)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다"));

        // JWT 토큰 생성
        String deviceId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(userId, deviceId, user.getTokenVersion());

        // RefreshToken 저장
        refreshTokenRepository.save(userId, deviceId, refreshToken);

        // 일회용 코드 발급 후 Redis 저장 (TTL 5분) — 토큰을 URL에 직접 노출하지 않음
        String code = UUID.randomUUID().toString();
        oAuth2CodeRepository.save(code, accessToken, refreshToken);

        log.info("OAuth2 로그인 성공 - 사용자 ID: {}", userId);

        // 프론트엔드로 리다이렉트 (일회용 코드만 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("code", code)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}