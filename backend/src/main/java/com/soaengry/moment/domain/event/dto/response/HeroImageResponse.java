package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.HeroImage;

import java.time.LocalDateTime;

public record HeroImageResponse(
        Long id,
        Long eventId,
        String imageUrl,
        String thumbnailUrl,
        Integer orderIndex,
        LocalDateTime createdAt
) {
    public static HeroImageResponse from(HeroImage heroImage) {
        return new HeroImageResponse(
                heroImage.getId(),
                heroImage.getEvent().getId(),
                heroImage.getImageUrl(),
                heroImage.getThumbnailUrl(),
                heroImage.getOrderIndex(),
                heroImage.getCreatedAt()
        );
    }
}
