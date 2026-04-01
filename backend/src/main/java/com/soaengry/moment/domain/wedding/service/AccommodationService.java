package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.wedding.dto.request.AccommodationRequest;
import com.soaengry.moment.domain.wedding.dto.response.AccommodationResponse;
import com.soaengry.moment.domain.wedding.entity.Accommodation;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.AccommodationRepository;
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
    private final WeddingService weddingService;

    public AccommodationResponse createAccommodation(Long weddingId, Long userId, AccommodationRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);
        Accommodation saved = accommodationRepository.save(request.toEntity(weddingId));
        return AccommodationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccommodationResponse> getAccommodationsByWedding(Long weddingId) {
        return accommodationRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(AccommodationResponse::from)
                .collect(Collectors.toList());
    }

    public AccommodationResponse updateAccommodation(Long accommodationId, Long userId, AccommodationRequest request) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOMMODATION_NOT_FOUND));

        weddingService.validateWeddingAccess(accommodation.getWeddingId(), userId);
        accommodation.update(request.name(), request.address(), request.phone(),
                request.distance(), request.priceRange(), request.orderIndex());
        return AccommodationResponse.from(accommodation);
    }

    public void deleteAccommodation(Long accommodationId, Long userId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOMMODATION_NOT_FOUND));

        weddingService.validateWeddingAccess(accommodation.getWeddingId(), userId);
        accommodationRepository.deleteById(accommodationId);
    }
}
