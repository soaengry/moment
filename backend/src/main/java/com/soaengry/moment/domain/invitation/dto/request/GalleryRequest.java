package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Gallery;

public record GalleryRequest(
        String imageUrl,
        String thumbnailUrl,
        String caption,
        Integer orderIndex
) {
    public Gallery toEntity(Long invitationId) {
        return Gallery.create(invitationId, imageUrl,
                thumbnailUrl != null ? thumbnailUrl : imageUrl,
                caption, orderIndex);
    }
}
