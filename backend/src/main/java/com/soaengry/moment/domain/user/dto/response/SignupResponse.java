package com.soaengry.moment.domain.user.dto.response;

public record SignupResponse(
        Long userId,
        String email,
        String verificationCode,
        String message
) {
}
