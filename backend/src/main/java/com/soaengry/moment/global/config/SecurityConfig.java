package com.soaengry.moment.global.config;

import com.soaengry.moment.global.security.JwtAuthenticationEntryPoint;
import com.soaengry.moment.global.security.JwtAuthenticationFilter;
import com.soaengry.moment.global.security.oauth2.CustomOAuth2UserService;
import com.soaengry.moment.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                }) // CorsConfig에서 설정
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/favicon.ico",
                                "/hc",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/env",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/check-email",
                                "/api/auth/check-email-verified",
                                "/api/auth/check-nickname",
                                "/api/auth/send-signup-verification",
                                "/api/auth/verification-status",
                                "/api/auth/oauth2/token",
                                "/api/users/restore",
                                "/api/banks/**",
                                "/ws/**",
                                "/login/oauth2/**",
                                "/oauth2/**"
                        ).permitAll()
                        // 초대장 공개 정보 조회 및 검색은 비인증 허용
                        .requestMatchers(HttpMethod.GET, "/api/events/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/*").permitAll()
                        // 방명록: 서비스에서 권한 검증
                        .requestMatchers(HttpMethod.GET, "/api/weddings/*/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/weddings/*/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/weddings/*/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/weddings/*/guestbook/**").permitAll()
                        // RSVP: 생성·조회는 공개, 수정·삭제·통계·목록은 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/rsvp").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rsvp/me").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/rsvp/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/rsvp/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/rsvp/stats").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/rsvp/list").authenticated()
                        // 채팅 메시지 조회는 참석자만 (서비스에서 attendance 검증)
                        .requestMatchers(HttpMethod.GET, "/api/events/*/chat/**").authenticated()
                        // 피드: GET은 공개, CUD는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/weddings/*/feed/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/weddings/*/feed/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/weddings/*/feed/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/weddings/*/feed/**").authenticated()
                        // 피드 조회는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/feed/**").authenticated()
                        // 채팅 이미지 업로드는 인증 사용자
                        .requestMatchers(HttpMethod.POST, "/api/weddings/*/chat/**").authenticated()
                        // 웨딩 하위 리소스 CUD는 인증 사용자 (서비스에서 소유자 권한 검증)
                        .requestMatchers(HttpMethod.POST, "/api/weddings/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/weddings/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/weddings/**").authenticated()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )
                // 인증 실패 처리
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}