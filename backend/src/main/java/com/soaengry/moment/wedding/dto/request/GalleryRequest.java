package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Gallery;

public record GalleryRequest(
        String imageUrl,
        String thumbnailUrl,
        String caption,
        Integer orderIndex
) {
    public Gallery toEntity(Long weddingId) {
        return Gallery.create(
                weddingId,
                imageUrl,
                thumbnailUrl,
                caption,
                orderIndex
        );
    }
}