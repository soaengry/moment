package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.wedding.dto.request.TransportationRequest;
import com.soaengry.moment.domain.wedding.dto.response.TransportationResponse;
import com.soaengry.moment.domain.wedding.entity.Transportation;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.TransportationRepository;
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
    private final WeddingService weddingService;

    public TransportationResponse createTransportation(Long weddingId, Long userId, TransportationRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);
        Transportation saved = transportationRepository.save(request.toEntity(weddingId));
        return TransportationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransportationResponse> getTransportationsByWedding(Long weddingId) {
        return transportationRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(TransportationResponse::from)
                .collect(Collectors.toList());
    }

    public TransportationResponse updateTransportation(Long transportationId, Long userId, TransportationRequest request) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.TRANSPORTATION_NOT_FOUND));

        weddingService.validateWeddingAccess(transportation.getWeddingId(), userId);
        transportation.update(request.type(), request.title(), request.description(), request.orderIndex());
        return TransportationResponse.from(transportation);
    }

    public void deleteTransportation(Long transportationId, Long userId) {
        Transportation transportation = transportationRepository.findById(transportationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.TRANSPORTATION_NOT_FOUND));

        weddingService.validateWeddingAccess(transportation.getWeddingId(), userId);
        transportationRepository.deleteById(transportationId);
    }
}
