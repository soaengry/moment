package com.soaengry.moment.domain.chat.dto.request;

public record ChatMessageRequest(
        Long weddingId,
        String content,
        String imageUrl,
        String type
) {}
