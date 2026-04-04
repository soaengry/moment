package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.invitation.dto.request.CoupleRequest;
import com.soaengry.moment.domain.invitation.dto.response.CoupleResponse;
import com.soaengry.moment.domain.invitation.entity.Couple;
import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.CoupleRepository;
import com.soaengry.moment.domain.invitation.repository.InvitationRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CoupleService {

    private final CoupleRepository coupleRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final InvitationService invitationService;

    public CoupleResponse createCouple(Long invitationId, Long userId, CoupleRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        Couple couple = request.toEntity(invitation);
        Couple saved = coupleRepository.save(couple);

        userRepository.findByEmail(saved.getEmail()).ifPresent(user -> {
            if (!attendanceRepository.existsByUserIdAndWeddingId(user.getId(), invitationId)) {
                attendanceRepository.save(Attendance.create(user.getId(), invitationId));
            }
        });

        return CoupleResponse.from(saved, invitationService.resolveCoupleUserId(saved.getEmail()));
    }

    @Transactional(readOnly = true)
    public List<CoupleResponse> getCouplesByInvitation(Long invitationId) {
        return coupleRepository.findByInvitationIdOrderByRole(invitationId).stream()
                .map(c -> CoupleResponse.from(c, invitationService.resolveCoupleUserId(c.getEmail())))
                .collect(Collectors.toList());
    }

    public CoupleResponse updateCouple(Long coupleId, Long userId, CoupleRequest request) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.COUPLE_NOT_FOUND));

        invitationService.validateInvitationAccess(couple.getInvitation().getId(), userId);

        couple.updateName(request.name());
        couple.updateFather(request.fatherName(), request.isFatherAlive());
        couple.updateMother(request.motherName(), request.isMotherAlive());
        couple.updateContact(request.contact());
        couple.updateProfileImageUrl(request.profileImageUrl());
        couple.updateIntroduction(request.introduction());

        return CoupleResponse.from(couple, invitationService.resolveCoupleUserId(couple.getEmail()));
    }

    public void deleteCouple(Long coupleId, Long userId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.COUPLE_NOT_FOUND));

        invitationService.validateInvitationAccess(couple.getInvitation().getId(), userId);
        coupleRepository.deleteById(coupleId);
    }
}
