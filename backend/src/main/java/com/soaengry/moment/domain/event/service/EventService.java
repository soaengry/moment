package com.soaengry.moment.domain.event.service;

import com.soaengry.moment.domain.event.dto.request.*;
import com.soaengry.moment.domain.event.dto.response.*;
import com.soaengry.moment.domain.event.entity.*;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.*;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.entity.AccountGroup;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.repository.*;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final HeroImageRepository heroImageRepository;
    private final TransportationRepository transportationRepository;
    private final AnnouncementRepository announcementRepository;
    private final WeddingRepository weddingRepository;
    private final HostRepository hostRepository;
    private final ScheduleRepository scheduleRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final KakaoGeocodingService kakaoGeocodingService;

    public Event validateAndGetEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUserId().equals(userId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }
        return event;
    }

    private KakaoGeocodingService.Coordinate resolveCoordinate(String address) {
        KakaoGeocodingService.Coordinate coord = kakaoGeocodingService.geocode(address);
        if (coord == null) {
            throw new EventException(EventErrorCode.GEOCODING_FAILED);
        }
        return coord;
    }

    private Long resolveUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElse(null);
    }

    public EventResponse createEvent(Long userId, EventRequest request) {
        if (eventRepository.existsBySlug(request.slug())) {
            throw new EventException(EventErrorCode.EVENT_SLUG_DUPLICATED);
        }
        KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.locationAddress());
        Event event = Event.builder()
                .userId(userId)
                .title(request.title())
                .type(request.type())
                .date(request.date())
                .locationName(request.locationName())
                .locationAddress(request.locationAddress())
                .locationDetail(request.locationDetail())
                .locationLat(coord.lat())
                .locationLng(coord.lng())
                .slug(request.slug())
                .build();
        return EventResponse.from(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        return EventResponse.from(event);
    }

    public EventResponse updateEvent(Long eventId, Long userId, EventRequest request) {
        Event event = validateAndGetEvent(eventId, userId);
        KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.locationAddress());
        event.updateTitle(request.title());
        event.updateDate(request.date());
        event.updateLocation(request.locationName(), request.locationAddress(), request.locationDetail(),
                coord.lat(), coord.lng());
        return EventResponse.from(event);
    }

    public void deleteEvent(Long eventId, Long userId) {
        validateAndGetEvent(eventId, userId);
        eventRepository.deleteById(eventId);
    }

    @Transactional(readOnly = true)
    public boolean checkSlugExists(String slug) {
        return eventRepository.existsBySlug(slug);
    }

    @Transactional(readOnly = true)
    public EventInfoResponse getEventInfo(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        Long eventId = event.getId();

        List<HeroImageResponse> heroImages = heroImageRepository.findByEventIdOrderByOrderIndex(eventId).stream()
                .map(HeroImageResponse::from)
                .toList();

        List<TransportationResponse> transportation = transportationRepository.findByEventIdOrderByOrderIndex(eventId).stream()
                .map(TransportationResponse::from)
                .toList();

        List<AnnouncementResponse> announcements = announcementRepository.findByEventIdOrderByIsPinnedDescCreatedAtDesc(eventId).stream()
                .map(AnnouncementResponse::from)
                .toList();

        // Wedding sub-resources (available for all event types)
        Wedding wedding = weddingRepository.findByEventId(eventId).orElse(null);

        if (wedding == null) {
            return new EventInfoResponse(EventResponse.from(event), heroImages, transportation, announcements,
                    null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        Long weddingId = wedding.getId();

        List<HostResponse> hosts = hostRepository.findByEventIdOrderByRole(eventId).stream()
                .map(h -> HostResponse.from(h, resolveUserIdByEmail(h.getEmail())))
                .toList();

        List<ScheduleResponse> schedules = scheduleRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(ScheduleResponse::from)
                .toList();

        List<AccountGroupWithAccountsResponse> accountGroups = accountGroupRepository
                .findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(group -> {
                    AccountGroupResponse groupResponse = AccountGroupResponse.from(group);
                    List<AccountResponse> accounts = accountRepository
                            .findByAccountGroupIdOrderByOrderIndex(group.getId()).stream()
                            .map(AccountResponse::from)
                            .toList();
                    return AccountGroupWithAccountsResponse.of(groupResponse, accounts);
                })
                .toList();

        return new EventInfoResponse(EventResponse.from(event), heroImages, transportation, announcements,
                WeddingResponse.from(wedding), hosts, schedules, accountGroups);
    }

    // ?�?�?� HeroImage ?�?�?�

    public HeroImageResponse addHeroImage(Long eventId, Long userId, HeroImageRequest request) {
        Event event = validateAndGetEvent(eventId, userId);
        HeroImage heroImage = HeroImage.builder()
                .event(event)
                .imageUrl(request.imageUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .orderIndex(request.orderIndex())
                .build();
        return HeroImageResponse.from(heroImageRepository.save(heroImage));
    }

    @Transactional(readOnly = true)
    public List<HeroImageResponse> getHeroImages(Long eventId) {
        return heroImageRepository.findByEventIdOrderByOrderIndex(eventId).stream()
                .map(HeroImageResponse::from)
                .toList();
    }

    public void deleteHeroImage(Long imageId, Long userId) {
        HeroImage heroImage = heroImageRepository.findById(imageId)
                .orElseThrow(() -> new EventException(EventErrorCode.HERO_IMAGE_NOT_FOUND));
        validateAndGetEvent(heroImage.getEvent().getId(), userId);
        heroImageRepository.deleteById(imageId);
    }

    // ?�?�?� Transportation ?�?�?�

    public TransportationResponse addTransportation(Long eventId, Long userId, TransportationRequest request) {
        validateAndGetEvent(eventId, userId);
        Transportation saved = transportationRepository.save(request.toEntity(eventId));
        return TransportationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransportationResponse> getTransportations(Long eventId) {
        return transportationRepository.findByEventIdOrderByOrderIndex(eventId).stream()
                .map(TransportationResponse::from)
                .toList();
    }

    public TransportationResponse updateTransportation(Long transportId, Long userId, TransportationRequest request) {
        Transportation transportation = transportationRepository.findById(transportId)
                .orElseThrow(() -> new EventException(EventErrorCode.TRANSPORTATION_NOT_FOUND));
        validateAndGetEvent(transportation.getEventId(), userId);
        transportation.update(request.type(), request.title(), request.description(), request.orderIndex());
        return TransportationResponse.from(transportation);
    }

    public void deleteTransportation(Long transportId, Long userId) {
        Transportation transportation = transportationRepository.findById(transportId)
                .orElseThrow(() -> new EventException(EventErrorCode.TRANSPORTATION_NOT_FOUND));
        validateAndGetEvent(transportation.getEventId(), userId);
        transportationRepository.deleteById(transportId);
    }

    // ?�?�?� Announcement ?�?�?�

    public AnnouncementResponse addAnnouncement(Long eventId, Long userId, AnnouncementRequest request) {
        validateAndGetEvent(eventId, userId);
        Announcement saved = announcementRepository.save(
                Announcement.create(eventId, request.title(), request.content(), request.isPinned()));
        return AnnouncementResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncements(Long eventId) {
        return announcementRepository.findByEventIdOrderByIsPinnedDescCreatedAtDesc(eventId).stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    public AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EventException(EventErrorCode.ANNOUNCEMENT_NOT_FOUND));
        validateAndGetEvent(announcement.getEventId(), userId);
        announcement.update(request.title(), request.content(), request.isPinned());
        return AnnouncementResponse.from(announcement);
    }

    public void deleteAnnouncement(Long announcementId, Long userId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EventException(EventErrorCode.ANNOUNCEMENT_NOT_FOUND));
        validateAndGetEvent(announcement.getEventId(), userId);
        announcementRepository.deleteById(announcementId);
    }
}
