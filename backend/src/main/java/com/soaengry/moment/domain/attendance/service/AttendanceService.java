package com.soaengry.moment.domain.attendance.service;

import com.soaengry.moment.domain.attendance.dto.request.AddAttendanceRequest;
import com.soaengry.moment.domain.attendance.dto.response.AttendanceResponse;
import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.exception.AttendanceErrorCode;
import com.soaengry.moment.domain.attendance.exception.AttendanceException;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import com.soaengry.moment.domain.invitation.entity.Couple;
import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.CoupleRepository;
import com.soaengry.moment.domain.invitation.repository.InvitationRepository;
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
    private final InvitationRepository invitationRepository;
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

        Map<Long, Invitation> weddingMap = invitationRepository.findAllById(weddingIds).stream()
                .collect(Collectors.toMap(Invitation::getId, w -> w));

        Map<Long, List<Couple>> coupleMap = coupleRepository.findByInvitationIdIn(weddingIds).stream()
                .collect(Collectors.groupingBy(c -> c.getInvitation().getId()));

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
        Invitation wedding = invitationRepository.findByInvitationId(request.invitationId())
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        Attendance attendance;
        try {
            attendance = attendanceRepository.save(Attendance.create(userId, wedding.getId()));
        } catch (DataIntegrityViolationException e) {
            throw new AttendanceException(AttendanceErrorCode.DUPLICATE_ATTENDANCE);
        }

        List<Couple> couples = coupleRepository.findByInvitationId(wedding.getId());

        return AttendanceResponse.from(attendance, wedding, couples);
    }

    @Transactional
    public void deleteAttendance(Long userId, Long attendanceId) {
        Attendance attendance = attendanceRepository.findByIdAndUserId(attendanceId, userId)
                .orElseThrow(() -> new AttendanceException(AttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        attendanceRepository.delete(attendance);
    }
}
