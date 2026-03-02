package com.soaengry.moment.domain.chat.controller;

import com.soaengry.moment.domain.chat.dto.request.ChatMessageRequest;
import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        ChatMessageResponse response = chatService.saveMessage(userId, request);
        messagingTemplate.convertAndSend("/topic/chat/wedding/" + request.weddingId(), response);
    }
}
