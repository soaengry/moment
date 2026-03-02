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
                                "/hc",
                                "/env",
                                "/api/auth/signup",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/check-email",
                                "/api/auth/check-nickname",
                                "/api/users/restore",
                                "/api/weddings/*/info",
                                "/api/banks/**",
                                "/ws/**",
                                "/login/oauth2/**",
                                "/oauth2/**"
                        ).permitAll()
                        // 방명록: GET은 공개, POST/PUT/DELETE는 인증 필요 (서비스에서 처리)
                        .requestMatchers(HttpMethod.GET, "/api/weddings/*/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/weddings/*/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/weddings/*/guestbook/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/weddings/*/guestbook/**").permitAll()
                        // 채팅 메시지 조회는 공개
                        .requestMatchers(HttpMethod.GET, "/api/weddings/*/chat/**").permitAll()
                        // 웨딩 피드: GET은 공개, CUD는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/weddings/*/feed/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/weddings/*/feed/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/weddings/*/feed/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/weddings/*/feed/**").authenticated()
                        // 피드 조회는 인증 필요
                        .requestMatchers(HttpMethod.GET, "/api/feed/**").authenticated()
                        // 채팅 이미지 업로드는 인증 사용자
                        .requestMatchers(HttpMethod.POST, "/api/weddings/*/chat/**").authenticated()
                        // 웨딩 생성은 ADMIN만, 하위 리소스 CUD는 인증 사용자 (서비스에서 권한 검증)
                        .requestMatchers(HttpMethod.POST, "/api/weddings").hasRole("ADMIN")
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