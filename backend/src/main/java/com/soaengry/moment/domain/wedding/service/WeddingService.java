package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.request.WeddingHostRequest;
import com.soaengry.moment.domain.wedding.dto.request.WeddingRequest;
import com.soaengry.moment.domain.wedding.dto.response.WeddingHostResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingResponse;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.entity.WeddingHost;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.WeddingHostRepository;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WeddingService {

    private final WeddingRepository weddingRepository;
    private final WeddingHostRepository weddingHostRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public void validateWeddingAccess(Long weddingId, Long userId) {
        validateAndGetWedding(weddingId, userId);
    }

    private Wedding validateAndGetWedding(Long weddingId, Long userId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        if (!wedding.getEvent().getUser().getId().equals(userId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_UNAUTHORIZED);
        }
        return wedding;
    }

    private Event validateAndGetEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUser().getId().equals(userId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }
        return event;
    }

    public Long resolveCoupleUserId(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElse(null);
    }

    // ─── Wedding CRUD ───

    public WeddingResponse createWedding(Long userId, WeddingRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUser().getId().equals(userId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }
        Wedding wedding = Wedding.builder()
                .event(event)
                .notice(request.notice())
                .parkingInfo(request.parkingInfo())
                .mealInfo(request.mealInfo())
                .greeting(request.greeting())
                .build();
        return WeddingResponse.from(weddingRepository.save(wedding));
    }

    @Transactional(readOnly = true)
    public WeddingResponse getWedding(Long weddingId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        return WeddingResponse.from(wedding);
    }

    @Transactional(readOnly = true)
    public WeddingResponse getWeddingByEventId(Long eventId) {
        Wedding wedding = weddingRepository.findByEventId(eventId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        return WeddingResponse.from(wedding);
    }

    public WeddingResponse updateWedding(Long weddingId, Long userId, WeddingRequest request) {
        Wedding wedding = validateAndGetWedding(weddingId, userId);
        wedding.update(request.notice(), request.parkingInfo(), request.mealInfo(), request.greeting());
        return WeddingResponse.from(wedding);
    }

    // ─── WeddingHost CRUD ───

    public WeddingHostResponse createWeddingHost(Long hostId, WeddingHostRequest request) {
        WeddingHost wh = WeddingHost.create(hostId,
                request.fatherName(), request.motherName(),
                request.isFatherAlive(), request.isMotherAlive());
        return WeddingHostResponse.from(weddingHostRepository.save(wh));
    }

    @Transactional(readOnly = true)
    public WeddingHostResponse getWeddingHost(Long hostId) {
        WeddingHost wh = weddingHostRepository.findByHostId(hostId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.HOST_NOT_FOUND));
        return WeddingHostResponse.from(wh);
    }

    public WeddingHostResponse updateWeddingHost(Long hostId, WeddingHostRequest request) {
        WeddingHost wh = weddingHostRepository.findByHostId(hostId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.HOST_NOT_FOUND));
        wh.update(request.fatherName(), request.motherName(),
                request.isFatherAlive(), request.isMotherAlive());
        return WeddingHostResponse.from(wh);
    }

    public void deleteWeddingHost(Long hostId) {
        weddingHostRepository.deleteByHostId(hostId);
    }
}
