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

    @Autowired private ChatService chatService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    /**
     * Client sends to: /app/chat.sendMessage
     * Server broadcasts to: /user/{recipientId}/queue/messages
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload SendMessageRequest request,
                            @AuthenticationPrincipal User sender) {
        if (sender == null) return;

        ChatMessageResponse message = chatService.sendMessage(sender.getId(), request.getRoomId(), request.getContent());

        // Determine recipient
        chatService.getRoomsForUser(sender.getId()).stream()
                .filter(r -> r.getId().equals(request.getRoomId()))
                .findFirst()
                .ifPresent(room -> {
                    Long recipientId = room.getUserOneId().equals(sender.getId())
                            ? room.getUserTwoId() : room.getUserOneId();
                    // Send to recipient
                    messagingTemplate.convertAndSendToUser(
                            recipientId.toString(),
                            "/queue/messages",
                            message
                    );
                    // Also send back to sender for confirmation
                    messagingTemplate.convertAndSendToUser(
                            sender.getId().toString(),
                            "/queue/messages",
                            message
                    );
                });
    }
}
