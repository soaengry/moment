package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.wedding.dto.request.CoupleRequest;
import com.soaengry.moment.domain.wedding.dto.response.CoupleResponse;
import com.soaengry.moment.domain.wedding.entity.Couple;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.CoupleRepository;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
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
    private final WeddingRepository weddingRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final WeddingService weddingService;

    public CoupleResponse createCouple(Long weddingId, Long userId, CoupleRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);

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

        return CoupleResponse.from(saved, weddingService.resolveCoupleUserId(saved.getEmail()));
    }

    @Transactional(readOnly = true)
    public List<CoupleResponse> getCouplesByWedding(Long weddingId) {
        return coupleRepository.findByWeddingIdOrderByRole(weddingId).stream()
                .map(c -> CoupleResponse.from(c, weddingService.resolveCoupleUserId(c.getEmail())))
                .collect(Collectors.toList());
    }

    public CoupleResponse updateCouple(Long coupleId, Long userId, CoupleRequest request) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.COUPLE_NOT_FOUND));

        weddingService.validateWeddingAccess(couple.getWedding().getId(), userId);

        couple.updateName(request.name());
        couple.updateFather(request.fatherName(), request.isFatherAlive());
        couple.updateMother(request.motherName(), request.isMotherAlive());
        couple.updateContact(request.contact());
        couple.updateProfileImageUrl(request.profileImageUrl());
        couple.updateIntroduction(request.introduction());

        return CoupleResponse.from(couple, weddingService.resolveCoupleUserId(couple.getEmail()));
    }

    public void deleteCouple(Long coupleId, Long userId) {
        Couple couple = coupleRepository.findById(coupleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.COUPLE_NOT_FOUND));

        weddingService.validateWeddingAccess(couple.getWedding().getId(), userId);
        coupleRepository.deleteById(coupleId);
    }
}
