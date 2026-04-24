package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.entity.RecurrenceType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequest(
        String title,
        @Pattern(
                regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9-]+$",
                message = "slug는 영문자를 최소 1개 포함해야 하며 영문, 숫자, '-'만 사용할 수 있습니다"
        )
        @Size(max = 50, message = "slug는 최대 50자입니다")
        String slug,
        EventType type,
        LocalDateTime date,
        String locationName,
        String locationAddress,
        String locationDetail,
        Boolean isPublic,

        String notice,
        String parkingInfo,
        String mealInfo,
        String greeting,

        RecurrenceType recurrenceType,
        String recurrenceDays,
        String recurrenceEndDate
) {
}
