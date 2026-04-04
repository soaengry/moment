package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.AccommodationRequest;
import com.soaengry.moment.domain.invitation.dto.response.AccommodationResponse;
import com.soaengry.moment.domain.invitation.entity.Accommodation;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final InvitationService invitationService;

    public AccommodationResponse createAccommodation(Long invitationId, Long userId, AccommodationRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);
        Accommodation saved = accommodationRepository.save(request.toEntity(invitationId));
        return AccommodationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponse> getAccommodationsByInvitation(Long invitationId) {
        return accommodationRepository.findByInvitationIdOrderByOrderIndex(invitationId).stream()
                .map(AccommodationResponse::from)
                .collect(Collectors.toList());
    }

    public AccommodationResponse updateAccommodation(Long accommodationId, Long userId, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOMMODATION_NOT_FOUND));

        invitationService.validateInvitationAccess(accommodation.getInvitationId(), userId);
        accommodation.update(request.name(), request.address(), request.phone(),
                request.distance(), request.priceRange(), request.orderIndex());
        return AccommodationResponse.from(accommodation);
    }

    public void deleteAccommodation(Long accommodationId, Long userId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOMMODATION_NOT_FOUND));

        invitationService.validateInvitationAccess(accommodation.getInvitationId(), userId);
        accommodationRepository.deleteById(accommodationId);
    }
}
