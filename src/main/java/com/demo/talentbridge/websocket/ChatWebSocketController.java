package com.demo.talentbridge.websocket;

import com.demo.talentbridge.dto.request.SendMessageRequest;
import com.demo.talentbridge.dto.response.ChatMessageResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request,
                            @AuthenticationPrincipal User sender) {
        if (sender == null || request == null) {
            return;
        }

        ChatMessageResponse message = chatService.sendMessage(
                sender.getId(),
                request.getRoomId(),
                request.getContent()
        );

        Long recipientId = chatService.resolveRecipientId(sender.getId(), request.getRoomId());

        messagingTemplate.convertAndSendToUser(
                recipientId.toString(),
                "/queue/messages",
                message
        );

        messagingTemplate.convertAndSendToUser(
                sender.getId().toString(),
                "/queue/messages",
                message
        );
    }
}