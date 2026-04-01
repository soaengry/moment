package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.request.WeddingRequest;
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
    public void validateWeddingAccess(Long weddingId, Long userId) {
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
    public Long resolveCoupleUserId(String email) {
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

    @Transactional(readOnly = true)
    public boolean checkInvitationIdExists(String invitationId) {
        return weddingRepository.existsByInvitationId(invitationId);
    }

    // 전체 정보 조회
    public WeddingInfoResponse getWeddingInfo(String invitationId) {
        Wedding entity = weddingRepository.findByInvitationId(invitationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        Long weddingId = entity.getId();

        WeddingResponse wedding = WeddingResponse.from(entity);

        List<CoupleResponse> couples = coupleRepository.findByWeddingIdOrderByRole(weddingId).stream()
                .map(couple -> CoupleResponse.from(couple, resolveCoupleUserId(couple.getEmail())))
                .collect(Collectors.toList());

        List<ScheduleResponse> schedules = scheduleRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());

        List<AccountGroupResponse> accountGroups = accountGroupRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(AccountGroupResponse::from)
                .collect(Collectors.toList());

        List<AccountGroupWithAccountsResponse> accountGroupsWithAccounts = accountGroups.stream()
                .map(group -> {
                    List<AccountResponse> accounts = accountRepository.findByAccountGroupIdOrderByOrderIndex(group.id()).stream()
                            .map(AccountResponse::from)
                            .collect(Collectors.toList());
                    return AccountGroupWithAccountsResponse.of(group, accounts);
                })
                .collect(Collectors.toList());

        List<GalleryResponse> gallery = galleryRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(GalleryResponse::from)
                .collect(Collectors.toList());

        List<TransportationResponse> transportation = transportationRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(TransportationResponse::from)
                .collect(Collectors.toList());

        List<AccommodationResponse> accommodation = accommodationRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(AccommodationResponse::from)
                .collect(Collectors.toList());

        List<AnnouncementResponse> announcements = announcementRepository.findByWeddingIdOrderByIsPinnedDescCreatedAtDesc(weddingId).stream()
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());

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
