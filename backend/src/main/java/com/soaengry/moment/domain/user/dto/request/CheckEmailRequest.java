package com.soaengry.moment.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CheckEmailRequest(
        @NotBlank
        @Email
        String email
) {
}
