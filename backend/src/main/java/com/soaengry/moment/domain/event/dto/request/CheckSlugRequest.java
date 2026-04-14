package com.soaengry.moment.domain.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CheckSlugRequest(
        @NotBlank(message = "slug는 필수입니다")
        @Pattern(
                regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9-]+$",
                message = "slug는 영문자를 최소 1개 포함해야 하며 영문, 숫자, '-'만 사용할 수 있습니다"
        )
        @Size(max = 50, message = "slug는 최대 50자입니다")
        String slug
) {}
