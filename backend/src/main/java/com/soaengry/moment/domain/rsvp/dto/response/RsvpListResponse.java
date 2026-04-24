package com.soaengry.moment.domain.rsvp.dto.response;

import java.util.List;

public record RsvpListResponse(
        List<RsvpResponse> items,
        long totalCount,
        int page,
        int size,
        boolean hasNext
) {}
