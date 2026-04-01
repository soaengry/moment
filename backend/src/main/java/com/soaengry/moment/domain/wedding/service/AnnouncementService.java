package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.wedding.dto.request.AnnouncementRequest;
import com.soaengry.moment.domain.wedding.dto.response.AnnouncementResponse;
import com.soaengry.moment.domain.wedding.entity.Announcement;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.AnnouncementRepository;
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
    private final WeddingService weddingService;

    public AnnouncementResponse createAnnouncement(Long weddingId, Long userId, AnnouncementRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);
        Announcement saved = announcementRepository.save(request.toEntity(weddingId));
        return AnnouncementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncementsByWedding(Long weddingId) {
        return announcementRepository.findByWeddingIdOrderByIsPinnedDescCreatedAtDesc(weddingId).stream()
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());
    }

    public AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ANNOUNCEMENT_NOT_FOUND));

        weddingService.validateWeddingAccess(announcement.getWeddingId(), userId);
        announcement.update(request.title(), request.content(), request.isPinned());
        return AnnouncementResponse.from(announcement);
    }

    public void deleteAnnouncement(Long announcementId, Long userId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ANNOUNCEMENT_NOT_FOUND));

        weddingService.validateWeddingAccess(announcement.getWeddingId(), userId);
        announcementRepository.deleteById(announcementId);
    }
}
