package com.soaengry.moment.domain.attendance.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AddAttendanceRequest(
        @NotBlank(message = "이벤트 슬러그는 필수입니다")
        String slug
) {
}
