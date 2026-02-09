package com.soaengry.moment.user.dto;

public record SignupResponse(
    Long userId, 
    String email, 
    String verificationCode, 
    String message
) {
}
