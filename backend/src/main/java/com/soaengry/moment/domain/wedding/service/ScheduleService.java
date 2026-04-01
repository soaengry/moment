package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.wedding.dto.request.ScheduleRequest;
import com.soaengry.moment.domain.wedding.dto.response.ScheduleResponse;
import com.soaengry.moment.domain.wedding.entity.Schedule;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final WeddingService weddingService;

    public ScheduleResponse createSchedule(Long weddingId, Long userId, ScheduleRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);
        Schedule saved = scheduleRepository.save(request.toEntity(weddingId));
        return ScheduleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByWedding(Long weddingId) {
        return scheduleRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    public ScheduleResponse updateSchedule(Long scheduleId, Long userId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));

        weddingService.validateWeddingAccess(schedule.getWeddingId(), userId);
        schedule.update(request.time(), request.title(), request.description(), request.orderIndex());
        return ScheduleResponse.from(schedule);
    }

    public void deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));

        weddingService.validateWeddingAccess(schedule.getWeddingId(), userId);
        scheduleRepository.deleteById(scheduleId);
    }
}
