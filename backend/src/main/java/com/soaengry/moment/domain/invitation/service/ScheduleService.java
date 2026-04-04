package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.ScheduleRequest;
import com.soaengry.moment.domain.invitation.dto.response.ScheduleResponse;
import com.soaengry.moment.domain.invitation.entity.Schedule;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.ScheduleRepository;
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
    private final InvitationService invitationService;

    public ScheduleResponse createSchedule(Long invitationId, Long userId, ScheduleRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);
        Schedule saved = scheduleRepository.save(request.toEntity(invitationId));
        return ScheduleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByInvitation(Long invitationId) {
        return scheduleRepository.findByInvitationIdOrderByOrderIndex(invitationId).stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    public ScheduleResponse updateSchedule(Long scheduleId, Long userId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.SCHEDULE_NOT_FOUND));

        invitationService.validateInvitationAccess(schedule.getInvitationId(), userId);
        schedule.update(request.time(), request.title(), request.description(), request.orderIndex());
        return ScheduleResponse.from(schedule);
    }

    public void deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.SCHEDULE_NOT_FOUND));

        invitationService.validateInvitationAccess(schedule.getInvitationId(), userId);
        scheduleRepository.deleteById(scheduleId);
    }
}
