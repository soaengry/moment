package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.GalleryRequest;
import com.soaengry.moment.domain.invitation.dto.response.GalleryResponse;
import com.soaengry.moment.domain.invitation.entity.Gallery;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final InvitationService invitationService;

    public GalleryResponse createGallery(Long invitationId, Long userId, GalleryRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);
        Gallery saved = galleryRepository.save(request.toEntity(invitationId));
        return GalleryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<GalleryResponse> getGalleriesByInvitation(Long invitationId) {
        return galleryRepository.findByInvitationIdOrderByOrderIndex(invitationId).stream()
                .map(GalleryResponse::from)
                .collect(Collectors.toList());
    }

    public GalleryResponse updateGallery(Long galleryId, Long userId, GalleryRequest request) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.GALLERY_NOT_FOUND));

        invitationService.validateInvitationAccess(gallery.getInvitationId(), userId);
        gallery.update(request.caption(), request.orderIndex());
        return GalleryResponse.from(gallery);
    }

    public void deleteGallery(Long galleryId, Long userId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.GALLERY_NOT_FOUND));

        invitationService.validateInvitationAccess(gallery.getInvitationId(), userId);
        galleryRepository.deleteById(galleryId);
    }
}
