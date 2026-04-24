package com.soaengry.moment.domain.event.service;

import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.event.dto.request.*;
import com.soaengry.moment.domain.event.dto.response.*;
import com.soaengry.moment.domain.event.entity.*;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.*;
import com.soaengry.moment.domain.feed.repository.PostRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.exception.UserErrorCode;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.response.WeddingDetailResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingHostCombinedResponse;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.entity.WeddingHost;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.WeddingHostRepository;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventService {

    private static final int MAX_ACCOUNT_GROUPS = 4;
    private static final int MAX_ACCOUNTS_PER_GROUP = 3;

    private final EventRepository eventRepository;
    private final HeroImageRepository heroImageRepository;
    private final TransportationRepository transportationRepository;
    private final AnnouncementRepository announcementRepository;
    private final WeddingRepository weddingRepository;
    private final HostRepository hostRepository;
    private final ScheduleRepository scheduleRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final AccountRepository accountRepository;
    private final WeddingHostRepository weddingHostRepository;
    private final UserRepository userRepository;
    private final KakaoGeocodingService kakaoGeocodingService;
    private final AttendanceRepository attendanceRepository;
    private final PostRepository postRepository;

    public Event validateAndGetEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUser().getId().equals(userId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }
        return event;
    }

    public void validateViewAccess(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        validateViewAccess(event, userId);
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
                .map(User::getId)
                .orElse(null);
    }

    @Transactional
    public EventResponse createEvent(Long userId, EventRequest request) {
        if (eventRepository.existsBySlug(request.slug())) {
            throw new EventException(EventErrorCode.EVENT_SLUG_DUPLICATED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        LocalDate recurrenceEndDate = request.recurrenceEndDate() != null && !request.recurrenceEndDate().isBlank()
                ? LocalDate.parse(request.recurrenceEndDate()) : null;

        Event.EventBuilder builder = Event.builder()
                .user(user)
                .title(request.title())
                .type(request.type())
                .date(request.date())
                .slug(request.slug())
                .isPublic(request.isPublic() != null && request.isPublic())
                .recurrenceType(request.recurrenceType())
                .recurrenceDays(request.recurrenceDays())
                .recurrenceEndDate(recurrenceEndDate);

        if (request.locationAddress() != null && !request.locationAddress().isBlank()) {
            KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.locationAddress());
            builder
                    .locationName(request.locationName())
                    .locationAddress(request.locationAddress())
                    .locationDetail(request.locationDetail())
                    .locationLat(coord.lat())
                    .locationLng(coord.lng());
        }
        Event event = builder.build();

        return EventResponse.from(eventRepository.save(event));
    }

    @Cacheable(cacheNames = "events", key = "#eventId")
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        return EventResponse.from(event);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "events", key = "#eventId")
    })
    @Transactional
    public EventResponse updateEvent(Long eventId, Long userId, EventRequest request) {
        Event event = validateAndGetEvent(eventId, userId);
        event.updateTitle(request.title());
        event.updateDate(request.date());
        if (request.locationAddress() != null && !request.locationAddress().isBlank()) {
            KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.locationAddress());
            event.updateLocation(request.locationName(), request.locationAddress(), request.locationDetail(),
                    coord.lat(), coord.lng());
        }
        event.updateIsPublic(request.isPublic() != null && request.isPublic());
        if (request.recurrenceType() != null) {
            LocalDate recurrenceEndDate = request.recurrenceEndDate() != null && !request.recurrenceEndDate().isBlank()
                    ? LocalDate.parse(request.recurrenceEndDate()) : null;
            event.updateRecurrence(request.recurrenceType(), request.recurrenceDays(), recurrenceEndDate);
        }
        return EventResponse.from(event);
    }

    @CacheEvict(cacheNames = "events", key = "#eventId")
    @Transactional
    public void deleteEvent(Long eventId, Long userId) {
        validateAndGetEvent(eventId, userId);

        // Posts soft-delete
        postRepository.softDeleteByEventId(eventId, LocalDateTime.now());

        // HeroImages
        heroImageRepository.deleteByEventId(eventId);

        // Hosts + WeddingHosts
        List<Long> hostIds = hostRepository.findByEventId(eventId)
                .stream().map(Host::getId).toList();
        if (!hostIds.isEmpty()) {
            weddingHostRepository.deleteByHostIdIn(hostIds);
            hostRepository.deleteByEventId(eventId);
        }

        // Schedules
        scheduleRepository.deleteByEventId(eventId);

        // AccountGroups + Accounts
        List<Long> groupIds = accountGroupRepository.findByEventIdOrderByOrderIndex(eventId)
                .stream().map(AccountGroup::getId).toList();
        if (!groupIds.isEmpty()) {
            accountRepository.deleteByAccountGroupIdIn(groupIds);
            accountGroupRepository.deleteByEventId(eventId);
        }

        // Transportation, Announcements
        transportationRepository.deleteByEventId(eventId);
        announcementRepository.deleteByEventId(eventId);

        // Wedding (FK event_id → events 이므로 event 전에 삭제)
        weddingRepository.findByEventId(eventId).ifPresent(weddingRepository::delete);

        eventRepository.deleteById(eventId);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<EventResponse> searchBySlug(String query, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return eventRepository
                .findBySlugContainingIgnoreCaseAndIsPublicTrueOrderByCreatedAtDesc(query.trim(), pageable)
                .map(EventResponse::from);
    }

    @Transactional(readOnly = true)
    public boolean checkSlugExists(String slug) {
        return eventRepository.existsBySlug(slug);
    }

    @Transactional(readOnly = true)
    public EventInfoResponse getEventInfoBySlug(String slug, Long userId) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        validateViewAccess(event, userId);
        return buildEventInfoResponse(event);
    }

    // 이미 로드된 Event 엔티티로 접근 권한을 검사한다 (findById 중복 호출 방지).
    private void validateViewAccess(Event event, Long userId) {
        if (event.isPublic()) return;
        if (userId == null) throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        if (event.getUser().getId().equals(userId)) return;
        if (attendanceRepository.existsByUserIdAndEventId(userId, event.getId())) return;
        throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
    }

    private EventInfoResponse buildEventInfoResponse(Event event) {
        Long eventId = event.getId();
        EventResponse eventResponse = EventResponse.from(event);

        List<HeroImageResponse> heroImages = getHeroImages(eventId);
        List<TransportationResponse> transportation = getTransportations(eventId);
        List<AnnouncementResponse> announcements = getAnnouncements(eventId);
        List<ScheduleResponse> schedules = getSchedules(eventId);
        List<AccountGroupWithAccountsResponse> accountGroups = buildAccountGroups(eventId);
        EventDetailResponse detail = getDetail(eventResponse);

        return new EventInfoResponse(eventResponse, heroImages, transportation,
                announcements, schedules, accountGroups, detail);
    }

    // accountGroupId 목록을 한 번에 조회해 N+1 쿼리를 방지한다.
    private List<AccountGroupWithAccountsResponse> buildAccountGroups(Long eventId) {
        List<AccountGroup> groups = accountGroupRepository.findByEventIdOrderByOrderIndex(eventId);
        if (groups.isEmpty()) return List.of();

        List<Long> groupIds = groups.stream().map(AccountGroup::getId).toList();
        Map<Long, List<Account>> accountsByGroupId = accountRepository
                .findByAccountGroupIdInOrderByOrderIndex(groupIds).stream()
                .collect(Collectors.groupingBy(Account::getAccountGroupId));

        return groups.stream()
                .map(group -> {
                    List<AccountResponse> accounts = accountsByGroupId
                            .getOrDefault(group.getId(), List.of()).stream()
                            .map(AccountResponse::from)
                            .toList();
                    return AccountGroupWithAccountsResponse.of(AccountGroupResponse.from(group), accounts);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public EventInfoResponse getEventInfo(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        validateViewAccess(event, userId);
        return buildEventInfoResponse(event);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getDetail(EventResponse event) {

        if (event.type() == EventType.WEDDING) {
            Wedding wedding = weddingRepository.findByEventId(event.id())
                    .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

            List<Host> hostEntities = hostRepository.findByEventId(event.id());
            List<WeddingHostCombinedResponse> hosts = buildWeddingHostResponses(hostEntities);

            return WeddingDetailResponse.builder()
                    .weddingId(wedding.getId())
                    .notice(wedding.getNotice())
                    .parkingInfo(wedding.getParkingInfo())
                    .mealInfo(wedding.getMealInfo())
                    .greeting(wedding.getGreeting())
                    .hosts(hosts)
                    .build();

        } else if (event.type() == EventType.GATHERING) {
            List<HostResponse> hosts = hostRepository.findByEventId(event.id()).stream()
                    .map(HostResponse::from)
                    .toList();

            return GatheringDetailResponse.builder()
                    .hosts(hosts)
                    .build();
        }

        return null;
    }

    // hostId 목록을 한 번에 조회해 N+1 쿼리를 방지한다.
    private List<WeddingHostCombinedResponse> buildWeddingHostResponses(List<Host> hosts) {
        if (hosts.isEmpty()) return List.of();

        List<Long> hostIds = hosts.stream().map(Host::getId).toList();
        Map<Long, WeddingHost> weddingHostByHostId = weddingHostRepository.findByHostIdIn(hostIds).stream()
                .collect(Collectors.toMap(WeddingHost::getHostId, wh -> wh));

        return hosts.stream()
                .map(host -> WeddingHostCombinedResponse.of(
                        HostResponse.from(host),
                        weddingHostByHostId.get(host.getId())))
                .toList();
    }

    // ─── HeroImage ───

    @Transactional
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

    @Transactional
    public void deleteHeroImage(Long imageId, Long userId) {
        HeroImage heroImage = heroImageRepository.findById(imageId)
                .orElseThrow(() -> new EventException(EventErrorCode.HERO_IMAGE_NOT_FOUND));
        validateAndGetEvent(heroImage.getEvent().getId(), userId);
        heroImageRepository.deleteById(imageId);
    }

    // ─── Transportation ───

    @Transactional
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

    @Transactional
    public TransportationResponse updateTransportation(Long transportId, Long userId, TransportationRequest request) {
        Transportation transportation = transportationRepository.findById(transportId)
                .orElseThrow(() -> new EventException(EventErrorCode.TRANSPORTATION_NOT_FOUND));
        validateAndGetEvent(transportation.getEventId(), userId);
        transportation.update(request.type(), request.title(), request.description(), request.orderIndex());
        return TransportationResponse.from(transportation);
    }

    @Transactional
    public void deleteTransportation(Long transportId, Long userId) {
        Transportation transportation = transportationRepository.findById(transportId)
                .orElseThrow(() -> new EventException(EventErrorCode.TRANSPORTATION_NOT_FOUND));
        validateAndGetEvent(transportation.getEventId(), userId);
        transportationRepository.deleteById(transportId);
    }

    // ─── Announcement ───

    @Transactional
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

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EventException(EventErrorCode.ANNOUNCEMENT_NOT_FOUND));
        validateAndGetEvent(announcement.getEventId(), userId);
        announcement.update(request.title(), request.content(), request.isPinned());
        return AnnouncementResponse.from(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, Long userId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new EventException(EventErrorCode.ANNOUNCEMENT_NOT_FOUND));
        validateAndGetEvent(announcement.getEventId(), userId);
        announcementRepository.deleteById(announcementId);
    }

    // ─── Schedule ───

    @Transactional
    public ScheduleResponse createSchedule(Long eventId, Long userId, ScheduleRequest request) {
        validateAndGetEvent(eventId, userId);
        Schedule saved = scheduleRepository.save(request.toEntity(eventId));
        return ScheduleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long eventId) {
        return scheduleRepository.findByEventIdOrderByOrderIndex(eventId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, Long userId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EventException(EventErrorCode.SCHEDULE_NOT_FOUND));
        validateAndGetEvent(schedule.getEventId(), userId);
        schedule.update(request.title(), request.description(), request.orderIndex());
        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EventException(EventErrorCode.SCHEDULE_NOT_FOUND));
        validateAndGetEvent(schedule.getEventId(), userId);
        scheduleRepository.deleteById(scheduleId);
    }

    // ─── Host ───

    @Transactional
    public HostResponse createHost(Long eventId, Long userId, HostRequest request) {
        validateAndGetEvent(eventId, userId);
        Host host = hostRepository.save(request.toEntity(eventId));
        if (request.weddingHostData() != null) {
            HostRequest.WeddingHostData wd = request.weddingHostData();
            weddingHostRepository.save(WeddingHost.create(host.getId(),
                    wd.fatherName(), wd.motherName(), wd.isFatherAlive(), wd.isMotherAlive()));
        }
        return HostResponse.from(host);
    }

    @Transactional(readOnly = true)
    public List<HostResponse> getHosts(Long eventId) {
        return hostRepository.findByEventIdOrderByRole(eventId).stream()
                .map(HostResponse::from)
                .toList();
    }

    @Transactional
    public HostResponse updateHost(Long hostId, Long userId, HostRequest request) {
        Host host = hostRepository.findById(hostId)
                .orElseThrow(() -> new EventException(EventErrorCode.HOST_NOT_FOUND));
        validateAndGetEvent(host.getEventId(), userId);
        host.updateName(request.name());
        host.updateContact(request.contact());
        host.updateProfileImageUrl(request.profileImageUrl());
        host.updateIntroduction(request.introduction());
        if (request.weddingHostData() != null) {
            HostRequest.WeddingHostData wd = request.weddingHostData();
            WeddingHost wh = weddingHostRepository.findByHostId(hostId)
                    .orElse(null);
            if (wh != null) {
                wh.update(wd.fatherName(), wd.motherName(), wd.isFatherAlive(), wd.isMotherAlive());
            } else {
                weddingHostRepository.save(WeddingHost.create(hostId,
                        wd.fatherName(), wd.motherName(), wd.isFatherAlive(), wd.isMotherAlive()));
            }
        }
        return HostResponse.from(host);
    }

    @Transactional
    public void deleteHost(Long hostId, Long userId) {
        Host host = hostRepository.findById(hostId)
                .orElseThrow(() -> new EventException(EventErrorCode.HOST_NOT_FOUND));
        validateAndGetEvent(host.getEventId(), userId);
        weddingHostRepository.deleteByHostId(hostId);
        hostRepository.deleteById(hostId);
    }

    @Transactional(readOnly = true)
    public List<WeddingHostCombinedResponse> getWeddingHosts(Long eventId) {
        return buildWeddingHostResponses(hostRepository.findByEventId(eventId));
    }

    @Transactional(readOnly = true)
    public List<HostResponse> getGatheringHosts(Long eventId) {
        return hostRepository.findByEventId(eventId).stream()
                .map(HostResponse::from)
                .toList();
    }

    // ─── AccountGroup ───

    @Transactional
    public AccountGroupResponse createAccountGroup(Long eventId, Long userId, AccountGroupRequest request) {
        validateAndGetEvent(eventId, userId);
        long count = accountGroupRepository.countByEventIdForUpdate(eventId);
        if (count >= MAX_ACCOUNT_GROUPS) {
            throw new EventException(EventErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED);
        }
        AccountGroup saved = accountGroupRepository.save(request.toEntity(eventId));
        return AccountGroupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountGroupResponse> getAccountGroups(Long eventId) {
        return accountGroupRepository.findByEventIdOrderByOrderIndex(eventId).stream()
                .map(AccountGroupResponse::from)
                .toList();
    }

    @Transactional
    public AccountGroupResponse updateAccountGroup(Long groupId, Long userId, AccountGroupRequest request) {
        AccountGroup group = accountGroupRepository.findById(groupId)
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetEvent(group.getEventId(), userId);
        group.update(request.groupName(), request.orderIndex());
        return AccountGroupResponse.from(group);
    }

    @Transactional
    public void deleteAccountGroup(Long groupId, Long userId) {
        AccountGroup group = accountGroupRepository.findById(groupId)
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetEvent(group.getEventId(), userId);
        accountGroupRepository.deleteById(groupId);
    }

    // ─── Account ───

    @Transactional
    public AccountResponse createAccount(Long groupId, Long userId, AccountRequest request) {
        AccountGroup group = accountGroupRepository.findById(groupId)
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        validateAndGetEvent(group.getEventId(), userId);

        long count = accountRepository.countByAccountGroupIdForUpdate(groupId);
        if (count >= MAX_ACCOUNTS_PER_GROUP) {
            throw new EventException(EventErrorCode.ACCOUNT_LIMIT_EXCEEDED);
        }

        // orderIndex 자동 부여
        Account saved = accountRepository.save(request.toEntity(groupId, (int) count));
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long groupId) {
        return accountRepository.findByAccountGroupIdOrderByOrderIndex(groupId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional
    public AccountResponse updateAccount(Long accountId, Long userId, AccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_NOT_FOUND));
        AccountGroup group = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetEvent(group.getEventId(), userId);
        account.update(request.bankName(), request.bankCode(), request.accountNumber(),
                request.accountHolder(), request.kakaoPayUrl(), request.orderIndex());
        return AccountResponse.from(account);
    }

    @Transactional
    public void deleteAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_NOT_FOUND));
        AccountGroup group = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new EventException(EventErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetEvent(group.getEventId(), userId);
        accountRepository.deleteById(accountId);
    }

    // ─── 복합 생성/수정 ───

    /**
     * Event + 모든 하위 도메인(heroImages, schedules, transportation, announcements,
     * hosts, weddingHosts, accountGroups, wedding)을 단일 트랜잭션으로 저장한다.
     * 중간에 어느 하나라도 실패하면 전체 롤백된다.
     */
    @CacheEvict(cacheNames = "events", key = "#eventId")
    @Transactional
    public EventInfoResponse updateEventWithDetails(Long eventId, Long userId, EventCreateRequest request) {
        Event event = validateAndGetEvent(eventId, userId);

        // 1. Event 기본 정보 업데이트
        event.updateTitle(request.event().title());
        event.updateDate(request.event().date());
        if (request.event().locationAddress() != null && !request.event().locationAddress().isBlank()) {
            KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.event().locationAddress());
            event.updateLocation(request.event().locationName(), request.event().locationAddress(),
                    request.event().locationDetail(), coord.lat(), coord.lng());
        }
        event.updateIsPublic(request.event().isPublic() != null && request.event().isPublic());

        // 2. WEDDING 전용 필드 업데이트
        if (event.getType() == EventType.WEDDING) {
            Wedding wedding = weddingRepository.findByEventId(eventId)
                    .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
            wedding.update(request.event().notice(), request.event().parkingInfo(),
                    request.event().mealInfo(), request.event().greeting());
        }

        // 3. HeroImages 교체
        heroImageRepository.deleteByEventId(eventId);
        List<HeroImageResponse> heroImages = Optional.ofNullable(request.heroImages())
                .orElse(Collections.emptyList()).stream()
                .map(r -> HeroImageResponse.from(heroImageRepository.save(
                        HeroImage.builder().event(event).imageUrl(r.imageUrl())
                                .thumbnailUrl(r.thumbnailUrl()).orderIndex(r.orderIndex()).build())))
                .toList();

        // 4. Hosts 교체 (WeddingHost → Host 순서로 삭제)
        List<Long> existingHostIds = hostRepository.findByEventId(eventId)
                .stream().map(Host::getId).toList();
        if (!existingHostIds.isEmpty()) {
            weddingHostRepository.deleteByHostIdIn(existingHostIds);
            hostRepository.deleteByEventId(eventId);
        }

        EventDetailResponse detail = null;
        if (event.getType() == EventType.WEDDING) {
            List<WeddingHostCombinedResponse> hosts = createHostsFromRequest(
                    eventId, userId, Optional.ofNullable(request.hosts()).orElse(Collections.emptyList()));
            Wedding wedding = weddingRepository.findByEventId(eventId)
                    .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
            detail = WeddingDetailResponse.builder()
                    .weddingId(wedding.getId()).notice(wedding.getNotice())
                    .parkingInfo(wedding.getParkingInfo()).mealInfo(wedding.getMealInfo())
                    .greeting(wedding.getGreeting()).hosts(hosts).build();
        } else if (event.getType() == EventType.GATHERING) {
            List<HostResponse> hosts = Optional.ofNullable(request.hosts())
                    .orElse(Collections.emptyList()).stream()
                    .map(h -> createHost(eventId, userId, h)).toList();
            detail = GatheringDetailResponse.builder().hosts(hosts).build();
        }

        // 5. Schedules 교체
        scheduleRepository.deleteByEventId(eventId);
        List<ScheduleResponse> schedules = Optional.ofNullable(request.schedules())
                .orElse(Collections.emptyList()).stream()
                .map(r -> ScheduleResponse.from(scheduleRepository.save(r.toEntity(eventId))))
                .toList();

        // 6. AccountGroups + Accounts 교체
        List<Long> existingGroupIds = accountGroupRepository.findByEventIdOrderByOrderIndex(eventId)
                .stream().map(AccountGroup::getId).toList();
        if (!existingGroupIds.isEmpty()) {
            accountRepository.deleteByAccountGroupIdIn(existingGroupIds);
            accountGroupRepository.deleteByEventId(eventId);
        }
        List<AccountGroupWithAccountsResponse> accountGroups =
                saveAccountGroupsFromRequest(eventId, request.accountGroups());

        // 7. Transportation 교체
        transportationRepository.deleteByEventId(eventId);
        List<TransportationResponse> transportation = Optional.ofNullable(request.transportation())
                .orElse(Collections.emptyList()).stream()
                .map(r -> TransportationResponse.from(transportationRepository.save(r.toEntity(eventId))))
                .toList();

        // 8. Announcements 교체
        announcementRepository.deleteByEventId(eventId);
        List<AnnouncementResponse> announcements = Optional.ofNullable(request.announcements())
                .orElse(Collections.emptyList()).stream()
                .map(r -> AnnouncementResponse.from(announcementRepository.save(
                        Announcement.create(eventId, r.title(), r.content(), r.isPinned()))))
                .toList();

        return new EventInfoResponse(EventResponse.from(event), heroImages, transportation,
                announcements, schedules, accountGroups, detail);
    }

    @Transactional
    public EventInfoResponse createEventWithDetails(Long userId, EventCreateRequest request) {
        Long eventId = createEvent(userId, request.event()).id();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        // HeroImages
        List<HeroImageResponse> heroImages = Optional.ofNullable(request.heroImages())
                .orElse(Collections.emptyList())
                .stream()
                .map(r -> addHeroImage(eventId, userId, r))
                .toList();

        // Schedules
        List<ScheduleResponse> schedules = Optional.ofNullable(request.schedules())
                .orElse(Collections.emptyList())
                .stream()
                .map(r -> createSchedule(eventId, userId, r))
                .toList();

        // Transportation
        List<TransportationResponse> transportation = Optional.ofNullable(request.transportation())
                .orElse(Collections.emptyList())
                .stream()
                .map(r -> addTransportation(eventId, userId, r))
                .toList();

        // Announcements
        List<AnnouncementResponse> announcements = Optional.ofNullable(request.announcements())
                .orElse(Collections.emptyList())
                .stream()
                .map(r -> addAnnouncement(eventId, userId, r))
                .toList();

        EventDetailResponse detail = null;

        if (event.getType() == EventType.WEDDING) {
            Wedding wedding = Wedding.builder()
                    .event(event)
                    .notice(request.event().notice())
                    .parkingInfo(request.event().parkingInfo())
                    .mealInfo(request.event().mealInfo())
                    .greeting(request.event().greeting())
                    .build();
            weddingRepository.save(wedding);

            List<WeddingHostCombinedResponse> hosts = createHostsFromRequest(
                    event.getId(), userId, Optional.ofNullable(request.hosts()).orElse(Collections.emptyList()));

            detail = WeddingDetailResponse.builder()
                    .weddingId(wedding.getId())
                    .notice(wedding.getNotice())
                    .parkingInfo(wedding.getParkingInfo())
                    .mealInfo(wedding.getMealInfo())
                    .greeting(wedding.getGreeting())
                    .hosts(hosts)
                    .build();
        }

        // 3. Gathering일 경우 Host만 생성
        else if (event.getType() == EventType.GATHERING) {
            List<HostResponse> hosts = request.hosts().stream()
                    .map(h -> createHost(event.getId(), userId, h))
                    .toList();

            detail = GatheringDetailResponse.builder()
                    .hosts(hosts)
                    .build();
        }

        // AccountGroups
        List<AccountGroupWithAccountsResponse> accountGroups =
                saveAccountGroupsFromRequest(eventId, request.accountGroups());

        return new EventInfoResponse(EventResponse.from(event),
                heroImages,
                transportation,
                announcements,
                schedules,
                accountGroups,
                detail);
    }

    private List<WeddingHostCombinedResponse> createHostsFromRequest(
            Long eventId, Long userId, List<HostRequest> hostRequests) {
        return hostRequests.stream()
                .map(h -> {
                    HostResponse hostResponse = createHost(eventId, userId, h);
                    WeddingHost weddingHost = weddingHostRepository.findByHostId(hostResponse.getId()).orElse(null);
                    return WeddingHostCombinedResponse.of(hostResponse, weddingHost);
                })
                .toList();
    }

    private List<AccountGroupWithAccountsResponse> saveAccountGroupsFromRequest(
            Long eventId, List<AccountGroupWithAccountsRequest> accountGroupRequests) {
        if (accountGroupRequests != null && accountGroupRequests.size() > MAX_ACCOUNT_GROUPS) {
            throw new EventException(EventErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED);
        }
        return Optional.ofNullable(accountGroupRequests)
                .orElse(Collections.emptyList())
                .stream()
                .map(groupReq -> {
                    AccountGroup group = accountGroupRepository.save(
                            AccountGroup.create(eventId, groupReq.groupName(), groupReq.orderIndex()));
                    List<AccountResponse> accounts = Optional.ofNullable(groupReq.accounts())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(accReq -> {
                                long count = accountRepository.countByAccountGroupIdForUpdate(group.getId());
                                if (count >= MAX_ACCOUNTS_PER_GROUP)
                                    throw new EventException(EventErrorCode.ACCOUNT_LIMIT_EXCEEDED);
                                return AccountResponse.from(accountRepository.save(accReq.toEntity(group.getId(), (int) count)));
                            })
                            .toList();
                    return AccountGroupWithAccountsResponse.of(AccountGroupResponse.from(group), accounts);
                })
                .toList();
    }
}
