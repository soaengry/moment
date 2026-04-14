package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.wedding.dto.request.GalleryRequest;
import com.soaengry.moment.domain.wedding.dto.response.GalleryResponse;
import com.soaengry.moment.domain.wedding.entity.Gallery;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.GalleryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GalleryService {

    private final GalleryRepository galleryRepository;
    private final WeddingService weddingService;

    public GalleryResponse createGallery(Long weddingId, Long userId, GalleryRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);
        Gallery saved = galleryRepository.save(request.toEntity(weddingId));
        return GalleryResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<GalleryResponse> getGalleriesByWedding(Long weddingId) {
        return galleryRepository.findByWeddingId(weddingId).stream()
                .map(GalleryResponse::from)
                .collect(Collectors.toList());
    }

    public GalleryResponse updateGallery(Long galleryId, Long userId, GalleryRequest request) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.GALLERY_NOT_FOUND));

        weddingService.validateWeddingAccess(gallery.getWeddingId(), userId);
        gallery.update(request.imageUrl(), request.thumbnailUrl());
        return GalleryResponse.from(gallery);
    }

    public void deleteGallery(Long galleryId, Long userId) {
        Gallery gallery = galleryRepository.findById(galleryId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.GALLERY_NOT_FOUND));

        weddingService.validateWeddingAccess(gallery.getWeddingId(), userId);
        galleryRepository.deleteById(galleryId);
    }
}
