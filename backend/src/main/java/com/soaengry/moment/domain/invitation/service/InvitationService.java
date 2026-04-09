package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.feed.repository.PostRepository;
import com.soaengry.moment.domain.invitation.dto.request.InvitationRequest;
import com.soaengry.moment.domain.invitation.dto.response.*;
import com.soaengry.moment.domain.invitation.entity.*;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.*;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
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
    private final PostRepository postRepository;

    private KakaoGeocodingService.Coordinate resolveCoordinate(String address) {
        KakaoGeocodingService.Coordinate coord = kakaoGeocodingService.geocode(address);
        if (coord == null) {
            throw new InvitationException(InvitationErrorCode.GEOCODING_FAILED);
        }
        return coord;
    }

    public void validateInvitationAccess(Long invitationId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED));

        if (user.getRole() == User.Role.ADMIN) return;

        if (coupleRepository.existsByInvitationIdAndEmail(invitationId, user.getEmail())) return;

        throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
    }

    public void validateViewAccess(Long invitationId, Long userId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        if (invitation.isPublic()) return;

        if (userId == null) throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED));

        if (user.getRole() == User.Role.ADMIN) return;

        if (attendanceRepository.existsByUserIdAndWeddingId(userId, invitationId)) return;

        if (coupleRepository.existsByInvitationIdAndEmail(invitationId, user.getEmail())) return;

        throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
    }

    public Long resolveCoupleUserId(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElse(null);
    }

    @Transactional
    public InvitationResponse createInvitation(InvitationRequest request) {
        KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.venueAddress());
        Invitation invitation = request.toEntity(coord.lat(), coord.lng());
        return InvitationResponse.from(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    public InvitationResponse getInvitation(Long invitationId, Long userId) {
        validateViewAccess(invitationId, userId);
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));
        return InvitationResponse.from(invitation);
    }

    @Transactional
    public InvitationResponse updateInvitation(Long invitationId, Long userId, InvitationRequest request) {
        validateInvitationAccess(invitationId, userId);

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        KakaoGeocodingService.Coordinate coord = resolveCoordinate(request.venueAddress());

        invitation.updateTitle(request.title());
        invitation.updateEventDate(request.eventDate());
        invitation.updateVenue(request.venueName(), request.venueAddress(), request.venueDetail(),
                coord.lat(), coord.lng(), request.venuePhone());
        invitation.updateDressCode(request.dressCode());
        invitation.updateNotice(request.notice());
        invitation.updateParkingInfo(request.parkingInfo());
        invitation.updateMealInfo(request.mealInfo());
        invitation.updateIsPublic(request.isPublic() != null && request.isPublic());

        return InvitationResponse.from(invitation);
    }

    @Transactional
    public void deleteInvitation(Long invitationId, Long userId) {
        validateInvitationAccess(invitationId, userId);

        if (!invitationRepository.existsById(invitationId)) {
            throw new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND);
        }
        postRepository.softDeleteByWeddingId(invitationId, LocalDateTime.now());
        invitationRepository.deleteById(invitationId);
    }

    @Transactional(readOnly = true)
    public boolean checkInvitationIdExists(String invitationId) {
        return invitationRepository.existsByInvitationId(invitationId);
    }

    @Transactional(readOnly = true)
    public InvitationInfoResponse getInvitationInfo(String invitationId, Long userId) {
        Invitation entity = invitationRepository.findByInvitationId(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));
        Long id = entity.getId();
        validateViewAccess(id, userId);

        InvitationResponse invitation = InvitationResponse.from(entity);

        List<CoupleResponse> couples = coupleRepository.findByInvitationIdOrderByRole(id).stream()
                .map(c -> CoupleResponse.from(c, resolveCoupleUserId(c.getEmail())))
                .collect(Collectors.toList());

        List<ScheduleResponse> schedules = scheduleRepository.findByInvitationIdOrderByOrderIndex(id).stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());

        List<AccountGroupWithAccountsResponse> accountGroups = accountGroupRepository
                .findByInvitationIdOrderByOrderIndex(id).stream()
                .map(group -> {
                    AccountGroupResponse groupResponse = AccountGroupResponse.from(group);
                    List<AccountResponse> accounts = accountRepository
                            .findByAccountGroupIdOrderByOrderIndex(group.getId()).stream()
                            .map(AccountResponse::from)
                            .collect(Collectors.toList());
                    return AccountGroupWithAccountsResponse.of(groupResponse, accounts);
                })
                .collect(Collectors.toList());

        List<GalleryResponse> gallery = galleryRepository.findByInvitationIdOrderByOrderIndex(id).stream()
                .map(GalleryResponse::from)
                .collect(Collectors.toList());

        List<TransportationResponse> transportation = transportationRepository
                .findByInvitationIdOrderByOrderIndex(id).stream()
                .map(TransportationResponse::from)
                .collect(Collectors.toList());

        List<AccommodationResponse> accommodation = accommodationRepository
                .findByInvitationIdOrderByOrderIndex(id).stream()
                .map(AccommodationResponse::from)
                .collect(Collectors.toList());

        List<AnnouncementResponse> announcements = announcementRepository
                .findByInvitationIdOrderByIsPinnedDescCreatedAtDesc(id).stream()
                .map(AnnouncementResponse::from)
                .collect(Collectors.toList());

        return InvitationInfoResponse.of(invitation, couples, schedules, accountGroups,
                gallery, transportation, accommodation, announcements);
    }
}
