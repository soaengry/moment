package com.soaengry.moment.domain.attendance.service;

import com.soaengry.moment.domain.attendance.dto.request.AddAttendanceRequest;
import com.soaengry.moment.domain.attendance.dto.response.AttendanceResponse;
import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.exception.AttendanceErrorCode;
import com.soaengry.moment.domain.attendance.exception.AttendanceException;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.event.entity.Host;
import com.soaengry.moment.domain.event.repository.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final HostRepository hostRepository;

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getMyAttendances(Long userId) {
        List<Attendance> attendances = attendanceRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (attendances.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = attendances.stream()
                .map(Attendance::getEventId)
                .toList();

        Map<Long, Event> eventMap = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, e -> e));

        Map<Long, List<Host>> hostMap = hostRepository.findByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(Host::getEventId));

        return attendances.stream()
                .filter(a -> eventMap.containsKey(a.getEventId()))
                .map(a -> {
                    Event event = eventMap.get(a.getEventId());
                    List<Host> hosts = hostMap.getOrDefault(a.getEventId(), List.of());
                    return AttendanceResponse.from(a, event, hosts);
                })
                .toList();
    }

    @Transactional
    public AttendanceResponse addAttendance(Long userId, AddAttendanceRequest request) {
        Event event = eventRepository.findBySlug(request.slug())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        Attendance attendance;
        try {
            attendance = attendanceRepository.save(Attendance.create(userId, event.getId()));
        } catch (DataIntegrityViolationException e) {
            throw new AttendanceException(AttendanceErrorCode.DUPLICATE_ATTENDANCE);
        }

        List<Host> hosts = hostRepository.findByEventId(event.getId());

        return AttendanceResponse.from(attendance, event, hosts);
    }

    @Transactional
    public void deleteAttendance(Long userId, Long attendanceId) {
        Attendance attendance = attendanceRepository.findByIdAndUserId(attendanceId, userId)
                .orElseThrow(() -> new AttendanceException(AttendanceErrorCode.ATTENDANCE_NOT_FOUND));

        attendanceRepository.delete(attendance);
    }
}
