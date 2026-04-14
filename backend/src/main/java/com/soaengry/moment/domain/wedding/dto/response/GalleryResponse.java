package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Gallery;

import java.time.LocalDateTime;

public record GalleryResponse(
        Long id,
        Long weddingId,
        String imageUrl,
        String thumbnailUrl,
        LocalDateTime createdAt
) {
    public static GalleryResponse from(Gallery gallery) {
        return new GalleryResponse(
                gallery.getId(),
                gallery.getWeddingId(),
                gallery.getImageUrl(),
                gallery.getThumbnailUrl(),
                gallery.getCreatedAt()
        );
    }
}