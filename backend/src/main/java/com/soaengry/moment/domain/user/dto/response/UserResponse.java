package com.soaengry.moment.domain.user.dto.response;

import com.soaengry.moment.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String role,
        String authProvider,
        Boolean isEmailVerified,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getAuthProvider().name(),
                user.getIsEmailVerified(),
                user.getCreatedAt()
        );
    }
}