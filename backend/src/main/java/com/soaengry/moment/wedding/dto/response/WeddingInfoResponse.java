package com.soaengry.moment.wedding.dto.response;

import java.util.List;

public record WeddingInfoResponse(
        WeddingResponse wedding,
        List<CoupleResponse> couples,
        List<ScheduleResponse> schedules,
        List<AccountGroupWithAccountsResponse> accountGroups,
        List<GalleryResponse> gallery,
        List<TransportationResponse> transportation,
        List<AccommodationResponse> accommodation,
        List<AnnouncementResponse> announcements
) {
    public static WeddingInfoResponse of(
            WeddingResponse wedding,
            List<CoupleResponse> couples,
            List<ScheduleResponse> schedules,
            List<AccountGroupWithAccountsResponse> accountGroups,
            List<GalleryResponse> gallery,
            List<TransportationResponse> transportation,
            List<AccommodationResponse> accommodation,
            List<AnnouncementResponse> announcements
    ) {
        return new WeddingInfoResponse(
                wedding,
                couples,
                schedules,
                accountGroups,
                gallery,
                transportation,
                accommodation,
                announcements
        );
    }
}