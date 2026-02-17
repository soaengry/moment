package com.soaengry.moment.wedding.service;

import com.soaengry.moment.wedding.dto.request.*;
import com.soaengry.moment.wedding.dto.response.*;
import com.soaengry.moment.wedding.entity.*;
import com.soaengry.moment.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.wedding.exception.WeddingException;
import com.soaengry.moment.wedding.repository.*;
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

    @Transactional
    public WeddingResponse createWedding(WeddingRequest request) {
        Wedding wedding = request.toEntity();
        Wedding saved = weddingRepository.save(wedding);
        return WeddingResponse.from(saved);
    }

    public WeddingResponse getWedding(Long weddingId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        return WeddingResponse.from(wedding);
    }

    @Transactional
    public WeddingResponse updateWedding(Long weddingId, WeddingRequest request) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        wedding.update(
                request.title(),
                request.weddingDate(),
                request.venueName(),
                request.venueAddress(),
                request.venueDetail(),
                request.venueLat(),
                request.venueLng(),
                request.venuePhone(),
                request.mapImageUrl(),
                request.dressCode(),
                request.notice(),
                request.parkingInfo(),
                request.mealInfo()
        );

        return WeddingResponse.from(wedding);
    }

    @Transactional
    public void deleteWedding(Long weddingId) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }
        weddingRepository.deleteById(weddingId);
    }

    // Couple CRUD
    @Transactional
    public CoupleResponse createCouple(Long weddingId, CoupleRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

        Couple couple = request.toEntity(weddingId);
        Couple saved = coupleRepository.save(couple);
        return CoupleResponse.from(saved);
    }

    public List<CoupleResponse> getCouplesByWedding(Long weddingId) {
        List<Couple> couples = coupleRepository.findByWeddingIdOrderByRole(weddingId);
        return couples.stream()
                .map(CoupleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CoupleResponse updateCouple(Long coupleId, CoupleRequest request) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.COUPLE_NOT_FOUND));

        couple.update(
                request.name(),
                request.fatherName(),
                request.motherName(),
                request.isFatherAlive(),
                request.isMotherAlive(),
                request.contact(),
                request.profileImageUrl(),
                request.introduction()
        );

        return CoupleResponse.from(couple);
    }

    @Transactional
    public void deleteCouple(Long coupleId) {
        if (!coupleRepository.existsById(coupleId)) {
            throw new WeddingException(WeddingErrorCode.COUPLE_NOT_FOUND);
        }
        coupleRepository.deleteById(coupleId);
    }

    // Schedule CRUD
    @Transactional
    public ScheduleResponse createSchedule(Long weddingId, ScheduleRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

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
    public ScheduleResponse updateSchedule(Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));

        schedule.update(
                request.time(),
                request.title(),
                request.description(),
                request.orderIndex()
        );

        return ScheduleResponse.from(schedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND);
        }
        scheduleRepository.deleteById(scheduleId);
    }

    // AccountGroup CRUD
    @Transactional
    public AccountGroupResponse createAccountGroup(Long weddingId, AccountGroupRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

        long count = accountGroupRepository.countByWeddingId(weddingId);
        if (count >= 3) {
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
    public AccountGroupResponse updateAccountGroup(Long accountGroupId, AccountGroupRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        accountGroup.update(
                request.side(),
                request.groupName(),
                request.orderIndex()
        );

        return AccountGroupResponse.from(accountGroup);
    }

    @Transactional
    public void deleteAccountGroup(Long accountGroupId) {
        if (!accountGroupRepository.existsById(accountGroupId)) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND);
        }
        accountGroupRepository.deleteById(accountGroupId);
    }

    // Account CRUD
    @Transactional
    public AccountResponse createAccount(Long accountGroupId, AccountRequest request) {
        if (!accountGroupRepository.existsById(accountGroupId)) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND);
        }

        long count = accountRepository.countByAccountGroupId(accountGroupId);
        if (count >= 2) {
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
    public AccountResponse updateAccount(Long accountId, AccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));

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
    public void deleteAccount(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND);
        }
        accountRepository.deleteById(accountId);
    }

    // Gallery CRUD
    @Transactional
    public GalleryResponse createGallery(Long weddingId, GalleryRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

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
    public GalleryResponse updateGallery(Long galleryId, GalleryRequest request) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.GALLERY_NOT_FOUND));

        gallery.update(
                request.caption(),
                request.orderIndex()
        );

        return GalleryResponse.from(gallery);
    }

    @Transactional
    public void deleteGallery(Long galleryId) {
        if (!galleryRepository.existsById(galleryId)) {
            throw new WeddingException(WeddingErrorCode.GALLERY_NOT_FOUND);
        }
        galleryRepository.deleteById(galleryId);
    }

    // Transportation CRUD
    @Transactional
    public TransportationResponse createTransportation(Long weddingId, TransportationRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

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
    public TransportationResponse updateTransportation(Long transportationId, TransportationRequest request) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.TRANSPORTATION_NOT_FOUND));

        transportation.update(
                request.type(),
                request.title(),
                request.description(),
                request.orderIndex()
        );

        return TransportationResponse.from(transportation);
    }

    @Transactional
    public void deleteTransportation(Long transportationId) {
        if (!transportationRepository.existsById(transportationId)) {
            throw new WeddingException(WeddingErrorCode.TRANSPORTATION_NOT_FOUND);
        }
        transportationRepository.deleteById(transportationId);
    }

    // Accommodation CRUD
    @Transactional
    public AccommodationResponse createAccommodation(Long weddingId, AccommodationRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

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
    public AccommodationResponse updateAccommodation(Long accommodationId, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOMMODATION_NOT_FOUND));

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
    public void deleteAccommodation(Long accommodationId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new WeddingException(WeddingErrorCode.ACCOMMODATION_NOT_FOUND);
        }
        accommodationRepository.deleteById(accommodationId);
    }

    // Announcement CRUD
    @Transactional
    public AnnouncementResponse createAnnouncement(Long weddingId, AnnouncementRequest request) {
        if (!weddingRepository.existsById(weddingId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND);
        }

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
    public AnnouncementResponse updateAnnouncement(Long announcementId, AnnouncementRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ANNOUNCEMENT_NOT_FOUND));

        announcement.update(
                request.title(),
                request.content(),
                request.isPinned()
        );

        return AnnouncementResponse.from(announcement);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        if (!announcementRepository.existsById(announcementId)) {
            throw new WeddingException(WeddingErrorCode.ANNOUNCEMENT_NOT_FOUND);
        }
        announcementRepository.deleteById(announcementId);
    }

    // 전체 정보 조회
    public WeddingInfoResponse getWeddingInfo(Long weddingId) {
        WeddingResponse wedding = getWedding(weddingId);
        List<CoupleResponse> couples = getCouplesByWedding(weddingId);
        List<ScheduleResponse> schedules = getSchedulesByWedding(weddingId);

        List<AccountGroupResponse> accountGroups = getAccountGroupsByWedding(weddingId);
        List<AccountGroupWithAccountsResponse> accountGroupsWithAccounts = accountGroups.stream()
                .map(group -> {
                    List<AccountResponse> accounts = getAccountsByGroup(group.id());
                    return AccountGroupWithAccountsResponse.of(group, accounts);
                })
                .collect(Collectors.toList());

        List<GalleryResponse> gallery = getGalleriesByWedding(weddingId);
        List<TransportationResponse> transportation = getTransportationsByWedding(weddingId);
        List<AccommodationResponse> accommodation = getAccommodationsByWedding(weddingId);
        List<AnnouncementResponse> announcements = getAnnouncementsByWedding(weddingId);

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