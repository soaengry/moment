package com.soaengry.moment.domain.feed.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostRequest(
        @NotBlank @Size(max = 200) String content,
        @Size(max = 4) List<String> imageUrls
) {}
