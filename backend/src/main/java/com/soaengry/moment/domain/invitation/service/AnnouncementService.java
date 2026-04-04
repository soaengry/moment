package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.AnnouncementRequest;
import com.soaengry.moment.domain.invitation.dto.response.AnnouncementResponse;
import com.soaengry.moment.domain.invitation.entity.Announcement;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final InvitationService invitationService;

    public AnnouncementResponse createAnnouncement(Long invitationId, Long userId, AnnouncementRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);
        Announcement saved = announcementRepository.save(request.toEntity(invitationId));
        return AnnouncementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncementsByInvitation(Long invitationId) {
        return announcementRepository.findByInvitationIdOrderByIsPinnedDescCreatedAtDesc(invitationId).stream()
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());
    }

    public AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ANNOUNCEMENT_NOT_FOUND));

        invitationService.validateInvitationAccess(announcement.getInvitationId(), userId);
        announcement.update(request.title(), request.content(), request.isPinned());
        return AnnouncementResponse.from(announcement);
    }

    public void deleteAnnouncement(Long announcementId, Long userId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ANNOUNCEMENT_NOT_FOUND));

        invitationService.validateInvitationAccess(announcement.getInvitationId(), userId);
        announcementRepository.deleteById(announcementId);
    }
}
