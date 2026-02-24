package com.soaengry.moment.domain.chat.controller;

import com.soaengry.moment.domain.chat.dto.request.ChatRoomRequest;
import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.dto.response.ChatRoomResponse;
import com.soaengry.moment.domain.chat.service.ChatService;
import com.soaengry.moment.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weddings/{weddingId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
            @PathVariable Long weddingId,
            @Valid @RequestBody ChatRoomRequest request) {
        ChatRoomResponse response = chatService.createRoom(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(@PathVariable Long weddingId) {
        List<ChatRoomResponse> responses = chatService.getRooms(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long weddingId,
            @PathVariable Long roomId,
            @PageableDefault(size = 50) Pageable pageable) {
        Page<ChatMessageResponse> responses = chatService.getMessages(roomId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
