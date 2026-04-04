package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.TransportationRequest;
import com.soaengry.moment.domain.invitation.dto.response.TransportationResponse;
import com.soaengry.moment.domain.invitation.entity.Transportation;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.TransportationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportationService {

    private final TransportationRepository transportationRepository;
    private final InvitationService invitationService;

    public TransportationResponse createTransportation(Long invitationId, Long userId, TransportationRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);
        Transportation saved = transportationRepository.save(request.toEntity(invitationId));
        return TransportationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransportationResponse> getTransportationsByInvitation(Long invitationId) {
        return transportationRepository.findByInvitationIdOrderByOrderIndex(invitationId).stream()
                .map(TransportationResponse::from)
                .collect(Collectors.toList());
    }

    public TransportationResponse updateTransportation(Long transportationId, Long userId, TransportationRequest request) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.TRANSPORTATION_NOT_FOUND));

        invitationService.validateInvitationAccess(transportation.getInvitationId(), userId);
        transportation.update(request.type(), request.title(), request.description(), request.orderIndex());
        return TransportationResponse.from(transportation);
    }

    public void deleteTransportation(Long transportationId, Long userId) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.TRANSPORTATION_NOT_FOUND));

        invitationService.validateInvitationAccess(transportation.getInvitationId(), userId);
        transportationRepository.deleteById(transportationId);
    }
}
