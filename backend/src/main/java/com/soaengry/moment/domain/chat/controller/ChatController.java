package com.soaengry.moment.domain.chat.controller;

import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/events/{eventId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long eventId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<ChatMessageResponse> responses = chatService.getMessages(eventId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/images")
    public ResponseEntity<Map<String, String>> uploadChatImage(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Long userId,
            @RequestParam("file") MultipartFile file) {
        String imageUrl = chatService.uploadChatImage(eventId, userId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}
