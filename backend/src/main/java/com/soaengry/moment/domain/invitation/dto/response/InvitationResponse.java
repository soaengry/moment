package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.entity.TemplateType;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long id,
        String title,
        String invitationId,
        TemplateType templateType,
        LocalDateTime eventDate,
        String venueName,
        String venueAddress,
        String venueDetail,
        Double venueLat,
        Double venueLng,
        String venuePhone,
        String dressCode,
        String notice,
        String parkingInfo,
        String mealInfo
) {
    public static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getTitle(),
                invitation.getInvitationId(),
                invitation.getTemplateType(),
                invitation.getEventDate(),
                invitation.getVenueName(),
                invitation.getVenueAddress(),
                invitation.getVenueDetail(),
                invitation.getVenueLat(),
                invitation.getVenueLng(),
                invitation.getVenuePhone(),
                invitation.getDressCode(),
                invitation.getNotice(),
                invitation.getParkingInfo(),
                invitation.getMealInfo()
        );
    }
}
