package com.soaengry.moment.wedding.dto.response;

import com.soaengry.moment.wedding.entity.Gallery;

import java.time.LocalDateTime;

public record GalleryResponse(
        Long id,
        Long weddingId,
        String imageUrl,
        String thumbnailUrl,
        String caption,
        Integer orderIndex,
        LocalDateTime createdAt
) {
    public static GalleryResponse from(Gallery gallery) {
        return new GalleryResponse(
                gallery.getId(),
                gallery.getWeddingId(),
                gallery.getImageUrl(),
                gallery.getThumbnailUrl(),
                gallery.getCaption(),
                gallery.getOrderIndex(),
                gallery.getCreatedAt()
        );
    }
}