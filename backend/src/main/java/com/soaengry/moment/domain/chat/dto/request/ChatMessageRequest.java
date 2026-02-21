package com.soaengry.moment.domain.chat.dto.request;

public record ChatMessageRequest(
        Long roomId,
        String content,
        String type
) {}
