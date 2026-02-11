package com.soaengry.moment.user.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn);
    }
}