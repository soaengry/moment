package com.soaengry.moment.domain.event.dto.request;

public record HeroImageRequest(
        String imageUrl,
        String thumbnailUrl,
        Integer orderIndex
) {}
