package com.soaengry.moment.domain.rsvp.service;

import com.soaengry.moment.domain.rsvp.dto.request.RsvpCreateRequest;
import com.soaengry.moment.domain.rsvp.dto.request.RsvpUpdateRequest;
import com.soaengry.moment.domain.rsvp.dto.response.RsvpListResponse;
import com.soaengry.moment.domain.rsvp.dto.response.RsvpResponse;
import com.soaengry.moment.domain.rsvp.dto.response.RsvpStatsResponse;
import com.soaengry.moment.domain.rsvp.entity.Rsvp;
import com.soaengry.moment.domain.rsvp.entity.RsvpAttendance;
import com.soaengry.moment.domain.rsvp.exception.RsvpErrorCode;
import com.soaengry.moment.domain.rsvp.exception.RsvpException;
import com.soaengry.moment.domain.rsvp.repository.RsvpRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final WeddingRepository weddingRepository;
    private final UserRepository userRepository;

    @Transactional
    public RsvpResponse create(RsvpCreateRequest request, Long userId) {
        Wedding wedding = findWedding(request.weddingId());

        if (userId != null && rsvpRepository.existsByWeddingIdAndUserId(request.weddingId(), userId)) {
            throw new RsvpException(RsvpErrorCode.RSVP_ALREADY_EXISTS);
        }

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        String sessionId = UUID.randomUUID().toString();

        Rsvp rsvp = Rsvp.builder()
                .wedding(wedding)
                .sessionId(sessionId)
                .user(user)
                .attendance(request.attendance())
                .name(request.name())
                .side(request.side())
                .phone(request.phone())
                .attendeeCount(request.attendeeCount())
                .willEat(request.meal().willEat())
                .mealCount(request.meal().mealCount())
                .willRide(request.shuttle().willRide())
                .rideCount(request.shuttle().rideCount())
                .note(request.note())
                .consent(request.consent())
                .build();

        return RsvpResponse.from(rsvpRepository.save(rsvp));
    }

    public RsvpResponse getMyRsvp(Long weddingId, Long userId) {
        if (userId == null) return null;
        return rsvpRepository.findByWeddingIdAndUserId(weddingId, userId)
                .map(RsvpResponse::from)
                .orElse(null);
    }

    @Transactional
    public RsvpResponse update(Long id, RsvpUpdateRequest request, Long userId) {
        Rsvp rsvp = findById(id);
        checkOwnership(rsvp, userId);

        rsvp.update(
                request.attendance(),
                request.name(),
                request.side(),
                request.phone(),
                request.attendeeCount(),
                request.meal().willEat(),
                request.meal().mealCount(),
                request.shuttle().willRide(),
                request.shuttle().rideCount(),
                request.note()
        );

        return RsvpResponse.from(rsvp);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Rsvp rsvp = findById(id);
        checkOwnership(rsvp, userId);
        rsvpRepository.delete(rsvp);
    }

    public RsvpStatsResponse getStats(Long weddingId, Long userId) {
        checkHostAccess(weddingId, userId);

        long totalRsvpCount = rsvpRepository.countByWeddingId(weddingId);
        long attendingCount = rsvpRepository.countByWeddingIdAndAttendance(weddingId, RsvpAttendance.YES);
        long totalAttendeeCount = rsvpRepository.sumAttendeeCountByWeddingIdAndAttendance(weddingId, RsvpAttendance.YES);
        long totalMealCount = rsvpRepository.sumMealCountByWeddingId(weddingId);
        long totalShuttleCount = rsvpRepository.sumRideCountByWeddingId(weddingId);

        return new RsvpStatsResponse(totalRsvpCount, attendingCount, totalAttendeeCount, totalMealCount, totalShuttleCount);
    }

    public RsvpListResponse getList(Long weddingId, int page, int size, Long userId) {
        checkHostAccess(weddingId, userId);

        Page<Rsvp> rsvpPage = rsvpRepository.findByWeddingIdOrderByCreatedAtDesc(
                weddingId, PageRequest.of(page, size));

        List<RsvpResponse> items = rsvpPage.getContent().stream()
                .map(RsvpResponse::from)
                .toList();

        return new RsvpListResponse(items, rsvpPage.getTotalElements(), page, size, rsvpPage.hasNext());
    }

    // ─── helpers ───

    private Wedding findWedding(Long weddingId) {
        return weddingRepository.findById(weddingId)
                .orElseThrow(() -> new RsvpException(RsvpErrorCode.WEDDING_NOT_FOUND));
    }

    private Rsvp findById(Long id) {
        return rsvpRepository.findById(id)
                .orElseThrow(() -> new RsvpException(RsvpErrorCode.RSVP_NOT_FOUND));
    }

    private void checkOwnership(Rsvp rsvp, Long userId) {
        if (userId == null || rsvp.getUser() == null || !rsvp.getUser().getId().equals(userId)) {
            throw new RsvpException(RsvpErrorCode.RSVP_UNAUTHORIZED);
        }
    }

    private void checkHostAccess(Long weddingId, Long userId) {
        if (userId == null) throw new RsvpException(RsvpErrorCode.HOST_ONLY);
        Wedding wedding = findWedding(weddingId);
        if (!wedding.getEvent().getUser().getId().equals(userId)) {
            throw new RsvpException(RsvpErrorCode.HOST_ONLY);
        }
    }
}
