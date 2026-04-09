package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.entity.TemplateType;

import java.time.LocalDateTime;

public record InvitationRequest(
        String title,
        String invitationId,
        TemplateType templateType,
        LocalDateTime eventDate,
        String venueName,
        String venueAddress,
        String venueDetail,
        String venuePhone,
        String dressCode,
        String notice,
        String parkingInfo,
        String mealInfo,
        Boolean isPublic
) {
    public Invitation toEntity(Double venueLat, Double venueLng) {
        return Invitation.builder()
                .title(title)
                .invitationId(invitationId)
                .templateType(templateType)
                .eventDate(eventDate)
                .venueName(venueName)
                .venueAddress(venueAddress)
                .venueDetail(venueDetail)
                .venueLat(venueLat)
                .venueLng(venueLng)
                .venuePhone(venuePhone)
                .dressCode(dressCode)
                .notice(notice)
                .parkingInfo(parkingInfo)
                .mealInfo(mealInfo)
                .isPublic(isPublic != null && isPublic)
                .build();
    }
}
