package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.ChatMessageResponse;
import com.demo.talentbridge.dto.response.ChatRoomResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    @Autowired private ChatService chatService;

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getRoomsForUser(user.getId())));
    }

    @PostMapping("/rooms/{otherUserId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getOrCreateRoom(
            @AuthenticationPrincipal User user, @PathVariable Long otherUserId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getOrCreateRoom(user.getId(), otherUserId)));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @AuthenticationPrincipal User user, @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getMessages(user.getId(), roomId)));
    }

    @PutMapping("/rooms/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@AuthenticationPrincipal User user, @PathVariable Long roomId) {
        chatService.markMessagesAsRead(user.getId(), roomId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }
}
