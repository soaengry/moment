package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Gallery;

public record GalleryRequest(
        String imageUrl,
        String thumbnailUrl
) {
    public Gallery toEntity(Long weddingId) {
        return Gallery.create(
                weddingId,
                imageUrl,
                thumbnailUrl != null ? thumbnailUrl : imageUrl
        );
    }
}