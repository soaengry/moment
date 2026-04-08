package com.soaengry.moment.domain.chat.dto.request;

public record ChatMessageRequest(
        Long eventId,
        String content,
        String imageUrl,
        String type
) {}
