package com.soaengry.moment.domain.guestbook.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GuestbookRequest(
        @NotBlank @Size(max = 50) String authorName,
        @NotBlank @Size(max = 500) String content,
        String password,
        Boolean isSecret
) {}
