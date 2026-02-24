package com.soaengry.moment.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckNicknameRequest(
        @NotBlank
        String nickname
) {
}
