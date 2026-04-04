package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Gallery;

import java.time.LocalDateTime;

public record GalleryResponse(
        Long id,
        Long invitationId,
        String imageUrl,
        String thumbnailUrl,
        String caption,
        Integer orderIndex,
        LocalDateTime createdAt
) {
    public static GalleryResponse from(Gallery gallery) {
        return new GalleryResponse(
                gallery.getId(),
                gallery.getInvitationId(),
                gallery.getImageUrl(),
                gallery.getThumbnailUrl(),
                gallery.getCaption(),
                gallery.getOrderIndex(),
                gallery.getCreatedAt()
        );
    }
}
