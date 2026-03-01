package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.entity.*;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.*;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeddingService {

    private final WeddingRepository weddingRepository;
    private final CoupleRepository coupleRepository;
    private final ScheduleRepository scheduleRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final AccountRepository accountRepository;
    private final GalleryRepository galleryRepository;
    private final TransportationRepository transportationRepository;
    private final AccommodationRepository accommodationRepository;
    private final AnnouncementRepository announcementRepository;
    private final KakaoGeocodingService kakaoGeocodingService;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    private KakaoGeocodingService.Coordinate resolveCoordinate(String address) {
        KakaoGeocodingService.Coordinate coord = kakaoGeocodingService.geocode(address);
        if (coord == null) {
            throw new WeddingException(WeddingErrorCode.GEOCODING_FAILED);
        }
        return coord;
    }

    /**
     * 웨딩 접근 권한 검증: ADMIN이거나 해당 웨딩의 커플(이메일 매칭)이면 통과
     */
    private void validateWeddingAccess(Long weddingId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_UNAUTHORIZED));

        if (user.getRole() == User.Role.ADMIN) {
            return;
        }

        if (coupleRepository.existsByWeddingIdAndEmail(weddingId, user.getEmail())) {
            return;
        }

        throw new WeddingException(WeddingErrorCode.WEDDING_UNAUTHORIZED);
    }

    /**
     * Couple의 이메일로 User를 조회하여 userId 반환
     */
    private Long resolveCoupleUserId(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    @Transactional
    public WeddingResponse createWedding(WeddingRequest request) {
        KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.venueAddress());
        Wedding wedding = request.toEntity(coord.lat(), coord.lng());
        Wedding saved = weddingRepository.save(wedding);
        return WeddingResponse.from(saved);
    }

    public WeddingResponse getWedding(Long weddingId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        return WeddingResponse.from(wedding);
    }

    @Transactional
    public WeddingResponse updateWedding(Long weddingId, Long userId, WeddingRequest request) {
        validateWeddingAccess(weddingId, userId);

        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.venueAddress());

        wedding.updateTitle(request.title());
        wedding.updateWeddingDate(request.weddingDate());
        wedding.updateVenue(
                request.venueName(),
                request.venueAddress(),
                request.venueDetail(),
                coord.lat(),
                coord.lng(),
                request.venuePhone());
        wedding.updateDressCode(request.dressCode());
        wedding.updateNotice(request.notice());
        wedding.updateParkingInfo(request.parkingInfo());
        wedding.updateMealInfo(request.mealInfo());

        return WeddingResponse.from(wedding);
    }

    @Transactional
    public void deleteWedding(Long weddingId, Long userId) {
        validateWeddingAccess(weddingId, userId);

        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }
        weddingRepository.deleteById(weddingId);
    }

    /**
     * 초대장 ID 중복 체크
     */
    @Transactional(readOnly = true)
    public boolean checkInvitationIdExists(String invitationId) {
        return weddingRepository.existsByInvitationId(invitationId);
    }

    // Couple CRUD
    @Transactional
    public CoupleResponse createCouple(Long weddingId, Long userId, CoupleRequest request) {
        validateWeddingAccess(weddingId, userId);

        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        Couple couple = request.toEntity(wedding);
        Couple saved = coupleRepository.save(couple);

        // 커플 이메일로 가입된 사용자가 있으면 자동 참석 등록
        userRepository.findByEmail(saved.getEmail()).ifPresent(user -> {
            if (!attendanceRepository.existsByUserIdAndWeddingId(user.getId(), weddingId)) {
                attendanceRepository.save(Attendance.create(user.getId(), weddingId));
            }
        });

        return CoupleResponse.from(saved, resolveCoupleUserId(saved.getEmail()));
    }

    public List<CoupleResponse> getCouplesByWedding(Long weddingId) {
        List<Couple> couples = coupleRepository.findByWeddingIdOrderByRole(weddingId);
        return couples.stream()
                .map(couple -> CoupleResponse.from(couple, resolveCoupleUserId(couple.getEmail())))
                .collect(Collectors.toList());
    }

    @Transactional
    public CoupleResponse updateCouple(Long coupleId, Long userId, CoupleRequest request) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.COUPLE_NOT_FOUND));

        validateWeddingAccess(couple.getWedding().getId(), userId);

        couple.updateName(request.name());
        couple.updateFather(request.fatherName(), request.isFatherAlive());
        couple.updateMother(request.motherName(), request.isMotherAlive());
        couple.updateContact(request.contact());
        couple.updateProfileImageUrl(request.profileImageUrl());
        couple.updateIntroduction(request.introduction());

        return CoupleResponse.from(couple, resolveCoupleUserId(couple.getEmail()));
    }

    @Transactional
    public void deleteCouple(Long coupleId, Long userId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.COUPLE_NOT_FOUND));

        validateWeddingAccess(couple.getWedding().getId(), userId);

        coupleRepository.deleteById(coupleId);
    }

    // Schedule CRUD
    @Transactional
    public ScheduleResponse createSchedule(Long weddingId, Long userId, ScheduleRequest request) {
        validateWeddingAccess(weddingId, userId);

        Schedule schedule = request.toEntity(weddingId);
        Schedule saved = scheduleRepository.save(schedule);
        return ScheduleResponse.from(saved);
    }

    public List<ScheduleResponse> getSchedulesByWedding(Long weddingId) {
        List<Schedule> schedules = scheduleRepository.findByWeddingIdOrderByOrderIndex(weddingId);
        return schedules.stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, Long userId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));

        validateWeddingAccess(schedule.getWeddingId(), userId);

        schedule.update(
                request.time(),
                request.title(),
                request.description(),
                request.orderIndex()
        );

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));

        validateWeddingAccess(schedule.getWeddingId(), userId);

        scheduleRepository.deleteById(scheduleId);
    }

    // AccountGroup CRUD
    @Transactional
    public AccountGroupResponse createAccountGroup(Long weddingId, Long userId, AccountGroupRequest request) {
        validateWeddingAccess(weddingId, userId);

        long count = accountGroupRepository.countByWeddingId(weddingId);
        if (count >= 4) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED);
        }

        AccountGroup accountGroup = request.toEntity(weddingId);
        AccountGroup saved = accountGroupRepository.save(accountGroup);
        return AccountGroupResponse.from(saved);
    }

    public List<AccountGroupResponse> getAccountGroupsByWedding(Long weddingId) {
        List<AccountGroup> groups = accountGroupRepository.findByWeddingIdOrderByOrderIndex(weddingId);
        return groups.stream()
                .map(AccountGroupResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountGroupResponse updateAccountGroup(Long accountGroupId, Long userId, AccountGroupRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        validateWeddingAccess(accountGroup.getWeddingId(), userId);

        accountGroup.update(
                request.side(),
                request.groupName(),
                request.orderIndex()
        );

        return AccountGroupResponse.from(accountGroup);
    }

    @Transactional
    public void deleteAccountGroup(Long accountGroupId, Long userId) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        validateWeddingAccess(accountGroup.getWeddingId(), userId);

        accountGroupRepository.deleteById(accountGroupId);
    }

    // Account CRUD
    @Transactional
    public AccountResponse createAccount(Long accountGroupId, Long userId, AccountRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        validateWeddingAccess(accountGroup.getWeddingId(), userId);

        long count = accountRepository.countByAccountGroupId(accountGroupId);
        if (count >= 3) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_LIMIT_EXCEEDED);
        }

        Account account = request.toEntity(accountGroupId);
        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }

    public List<AccountResponse> getAccountsByGroup(Long accountGroupId) {
        List<Account> accounts = accountRepository.findByAccountGroupIdOrderByOrderIndex(accountGroupId);
        return accounts.stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse updateAccount(Long accountId, Long userId, AccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));

        AccountGroup accountGroup = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        validateWeddingAccess(accountGroup.getWeddingId(), userId);

        account.update(
                request.bankName(),
                request.bankCode(),
                request.accountNumber(),
                request.accountHolder(),
                request.kakaoPayUrl(),
                request.orderIndex()
        );

        return AccountResponse.from(account);
    }

    @Transactional
    public void deleteAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));

        AccountGroup accountGroup = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        validateWeddingAccess(accountGroup.getWeddingId(), userId);

        accountRepository.deleteById(accountId);
    }

    // Gallery CRUD
    @Transactional
    public GalleryResponse createGallery(Long weddingId, Long userId, GalleryRequest request) {
        validateWeddingAccess(weddingId, userId);

        Gallery gallery = request.toEntity(weddingId);
        Gallery saved = galleryRepository.save(gallery);
        return GalleryResponse.from(saved);
    }

    public List<GalleryResponse> getGalleriesByWedding(Long weddingId) {
        List<Gallery> galleries = galleryRepository.findByWeddingIdOrderByOrderIndex(weddingId);
        return galleries.stream()
                .map(GalleryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public GalleryResponse updateGallery(Long galleryId, Long userId, GalleryRequest request) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.GALLERY_NOT_FOUND));

        validateWeddingAccess(gallery.getWeddingId(), userId);

        gallery.update(
                request.caption(),
                request.orderIndex()
        );

        return GalleryResponse.from(gallery);
    }

    @Transactional
    public void deleteGallery(Long galleryId, Long userId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.GALLERY_NOT_FOUND));

        validateWeddingAccess(gallery.getWeddingId(), userId);

        galleryRepository.deleteById(galleryId);
    }

    // Transportation CRUD
    @Transactional
    public TransportationResponse createTransportation(Long weddingId, Long userId, TransportationRequest request) {
        validateWeddingAccess(weddingId, userId);

        Transportation transportation = request.toEntity(weddingId);
        Transportation saved = transportationRepository.save(transportation);
        return TransportationResponse.from(saved);
    }

    public List<TransportationResponse> getTransportationsByWedding(Long weddingId) {
        List<Transportation> transportations = transportationRepository.findByWeddingIdOrderByOrderIndex(weddingId);
        return transportations.stream()
                .map(TransportationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransportationResponse updateTransportation(Long transportationId, Long userId, TransportationRequest request) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.TRANSPORTATION_NOT_FOUND));

        validateWeddingAccess(transportation.getWeddingId(), userId);

        transportation.update(
                request.type(),
                request.title(),
                request.description(),
                request.orderIndex()
        );

        return TransportationResponse.from(transportation);
    }

    @Transactional
    public void deleteTransportation(Long transportationId, Long userId) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.TRANSPORTATION_NOT_FOUND));

        validateWeddingAccess(transportation.getWeddingId(), userId);

        transportationRepository.deleteById(transportationId);
    }

    // Accommodation CRUD
    @Transactional
    public AccommodationResponse createAccommodation(Long weddingId, Long userId, AccommodationRequest request) {
        validateWeddingAccess(weddingId, userId);

        Accommodation accommodation = request.toEntity(weddingId);
        Accommodation saved = accommodationRepository.save(accommodation);
        return AccommodationResponse.from(saved);
    }

    public List<AccommodationResponse> getAccommodationsByWedding(Long weddingId) {
        List<Accommodation> accommodations = accommodationRepository.findByWeddingIdOrderByOrderIndex(weddingId);
        return accommodations.stream()
                .map(AccommodationResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccommodationResponse updateAccommodation(Long accommodationId, Long userId, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOMMODATION_NOT_FOUND));

        validateWeddingAccess(accommodation.getWeddingId(), userId);

        accommodation.update(
                request.name(),
                request.address(),
                request.phone(),
                request.distance(),
                request.priceRange(),
                request.orderIndex()
        );

        return AccommodationResponse.from(accommodation);
    }

    @Transactional
    public void deleteAccommodation(Long accommodationId, Long userId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOMMODATION_NOT_FOUND));

        validateWeddingAccess(accommodation.getWeddingId(), userId);

        accommodationRepository.deleteById(accommodationId);
    }

    // Announcement CRUD
    @Transactional
    public AnnouncementResponse createAnnouncement(Long weddingId, Long userId, AnnouncementRequest request) {
        validateWeddingAccess(weddingId, userId);

        Announcement announcement = request.toEntity(weddingId);
        Announcement saved = announcementRepository.save(announcement);
        return AnnouncementResponse.from(saved);
    }

    public List<AnnouncementResponse> getAnnouncementsByWedding(Long weddingId) {
        List<Announcement> announcements = announcementRepository.findByWeddingIdOrderByIsPinnedDescCreatedAtDesc(weddingId);
        return announcements.stream()
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long announcementId, Long userId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ANNOUNCEMENT_NOT_FOUND));

        validateWeddingAccess(announcement.getWeddingId(), userId);

        announcement.update(
                request.title(),
                request.content(),
                request.isPinned()
        );

        return AnnouncementResponse.from(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId, Long userId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ANNOUNCEMENT_NOT_FOUND));

        validateWeddingAccess(announcement.getWeddingId(), userId);

        announcementRepository.deleteById(announcementId);
    }

    // 전체 정보 조회
    public WeddingInfoResponse getWeddingInfo(String invitationId) {
        Wedding entity = weddingRepository.findByInvitationId(invitationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        WeddingResponse wedding = WeddingResponse.from(entity);
        List<CoupleResponse> couples = getCouplesByWedding(wedding.id());
        List<ScheduleResponse> schedules = getSchedulesByWedding(wedding.id());

        List<AccountGroupResponse> accountGroups = getAccountGroupsByWedding(wedding.id());
        List<AccountGroupWithAccountsResponse> accountGroupsWithAccounts = accountGroups.stream()
                .map(group -> {
                    List<AccountResponse> accounts = getAccountsByGroup(group.id());
                    return AccountGroupWithAccountsResponse.of(group, accounts);
                })
                .collect(Collectors.toList());

        List<GalleryResponse> gallery = getGalleriesByWedding(wedding.id());
        List<TransportationResponse> transportation = getTransportationsByWedding(wedding.id());
        List<AccommodationResponse> accommodation = getAccommodationsByWedding(wedding.id());
        List<AnnouncementResponse> announcements = getAnnouncementsByWedding(wedding.id());

        return WeddingInfoResponse.of(
                wedding,
                couples,
                schedules,
                accountGroupsWithAccounts,
                gallery,
                transportation,
                accommodation,
                announcements
        );
    }
}
