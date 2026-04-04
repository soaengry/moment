package com.soaengry.moment.domain.invitation.dto.response;

import java.util.List;

public record InvitationInfoResponse(
        InvitationResponse invitation,
        List<CoupleResponse> couples,
        List<ScheduleResponse> schedules,
        List<AccountGroupWithAccountsResponse> accountGroups,
        List<GalleryResponse> gallery,
        List<TransportationResponse> transportation,
        List<AccommodationResponse> accommodation,
        List<AnnouncementResponse> announcements
) {
    public static InvitationInfoResponse of(
            InvitationResponse invitation,
            List<CoupleResponse> couples,
            List<ScheduleResponse> schedules,
            List<AccountGroupWithAccountsResponse> accountGroups,
            List<GalleryResponse> gallery,
            List<TransportationResponse> transportation,
            List<AccommodationResponse> accommodation,
            List<AnnouncementResponse> announcements
    ) {
        return new InvitationInfoResponse(
                invitation, couples, schedules, accountGroups,
                gallery, transportation, accommodation, announcements
        );
    }
}
