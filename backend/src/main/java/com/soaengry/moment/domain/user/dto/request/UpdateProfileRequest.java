package com.soaengry.moment.domain.user.dto.request;

public record UpdateProfileRequest(
        String nickname,
        String profileImageUrl
) {
}
