package com.soaengry.moment.domain.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddAttendanceRequest(
        @NotBlank(message = "초대장 ID는 필수입니다")
        String invitationId
) {
}
