package com.soaengry.moment.domain.attendance.service;

import com.soaengry.moment.domain.attendance.dto.request.AddAttendanceRequest;
import com.soaengry.moment.domain.attendance.dto.response.AttendanceResponse;
import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.exception.AttendanceErrorCode;
import com.soaengry.moment.domain.attendance.exception.AttendanceException;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.wedding.entity.Couple;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.CoupleRepository;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WeddingRepository weddingRepository;
    private final CoupleRepository coupleRepository;

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getMyAttendances(Long userId) {
        List<Attendance> attendances = attendanceRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (attendances.isEmpty()) {
            return List.of();
        }

        List<Long> weddingIds = attendances.stream()
                .map(Attendance::getWeddingId)
                .toList();

        Map<Long, Wedding> weddingMap = weddingRepository.findAllById(weddingIds).stream()
                .collect(Collectors.toMap(Wedding::getId, w -> w));

        Map<Long, List<Couple>> coupleMap = weddingIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        coupleRepository::findByWeddingId
                ));

        return attendances.stream()
                .filter(a -> weddingMap.containsKey(a.getWeddingId()))
                .map(a -> AttendanceResponse.from(
                        a,
                        weddingMap.get(a.getWeddingId()),
                        coupleMap.getOrDefault(a.getWeddingId(), List.of())
                ))
                .toList();
    }

    @Transactional
    public AttendanceResponse addAttendance(Long userId, AddAttendanceRequest request) {
        Wedding wedding = weddingRepository.findByInvitationId(request.invitationId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        if (attendanceRepository.existsByUserIdAndWeddingId(userId, wedding.getId())) {
            throw new AttendanceException(AttendanceErrorCode.DUPLICATE_ATTENDANCE);
        }

        Attendance attendance = Attendance.create(userId, wedding.getId());
        attendanceRepository.save(attendance);

        List<Couple> couples = coupleRepository.findByWeddingId(wedding.getId());

        return AttendanceResponse.from(attendance, wedding, couples);
    }

    @Transactional
    public void deleteAttendance(Long userId, Long attendanceId) {
        Attendance attendance = attendanceRepository.findByIdAndUserId(attendanceId, userId)
                .orElseThrow(() -> new AttendanceException(AttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        attendanceRepository.delete(attendance);
    }
}
