package com.soaengry.moment.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRoomRequest(
        @NotBlank @Size(max = 100) String name
) {}
