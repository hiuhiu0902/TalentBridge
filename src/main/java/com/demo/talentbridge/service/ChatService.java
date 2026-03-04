package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.ChatMessageResponse;
import com.demo.talentbridge.dto.response.ChatRoomResponse;

import java.util.List;

public interface ChatService {
    ChatRoomResponse getOrCreateRoom(Long userOneId, Long userTwoId);
    List<ChatRoomResponse> getRoomsForUser(Long userId);
    List<ChatMessageResponse> getMessages(Long userId, Long roomId);
    ChatMessageResponse sendMessage(Long senderId, Long roomId, String content);
    void markMessagesAsRead(Long userId, Long roomId);
}
