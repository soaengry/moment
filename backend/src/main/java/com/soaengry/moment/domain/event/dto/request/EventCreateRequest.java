package com.soaengry.moment.domain.event.dto.request;

import java.util.List;

/**
 * event + 모든 하위 도메인을 단일 요청으로 생성하기 위한 복합 요청 DTO.
 * <p>
 * - heroImages / schedules / transportation / announcements / hosts / accountGroups : event 직속
 * - WEDDING 타입 전용 필드(notice, parkingInfo, mealInfo, greeting)는 EventRequest에 포함
 * - hosts 내 weddingHostData : WEDDING 타입에서만 사용 (fatherName, motherName 등)
 */
public record EventCreateRequest(
        // ─── Event 기본 정보 (WEDDING 전용 필드 포함) ───
        @jakarta.validation.Valid EventRequest event,

        // ─── Event 직속 하위 리소스 (모두 optional) ───
        List<HeroImageRequest> heroImages,
        List<ScheduleRequest> schedules,
        List<AccountGroupWithAccountsRequest> accountGroups,
        List<TransportationRequest> transportation,
        List<AnnouncementRequest> announcements,
        List<HostRequest> hosts
) {
}
