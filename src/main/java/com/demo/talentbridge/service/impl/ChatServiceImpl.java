package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.ChatMessageResponse;
import com.demo.talentbridge.dto.response.ChatRoomResponse;
import com.demo.talentbridge.entity.*;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.*;
import com.demo.talentbridge.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired private ChatRoomRepository chatRoomRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private UserRepository userRepository;

    @Override
    @Transactional
    public ChatRoomResponse getOrCreateRoom(Long userOneId, Long userTwoId) {
        return chatRoomRepository.findByUsers(userOneId, userTwoId)
                .map(this::mapRoomToResponse)
                .orElseGet(() -> {
                    User u1 = userRepository.findById(userOneId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userOneId));
                    User u2 = userRepository.findById(userTwoId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userTwoId));
                    ChatRoom room = ChatRoom.builder().userOne(u1).userTwo(u2).build();
                    return mapRoomToResponse(chatRoomRepository.save(room));
                });
    }

    @Override
    public List<ChatRoomResponse> getRoomsForUser(Long userId) {
        return chatRoomRepository.findByUserId(userId).stream()
                .map(this::mapRoomToResponse).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResponse> getMessages(Long userId, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
        if (!room.getUserOne().getId().equals(userId) && !room.getUserTwo().getId().equals(userId)) {
            throw new UnauthorizedException("You are not a member of this chat room");
        }
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId).stream()
                .map(this::mapMsgToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, Long roomId, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", senderId));
        if (!room.getUserOne().getId().equals(senderId) && !room.getUserTwo().getId().equals(senderId)) {
            throw new UnauthorizedException("You are not a member of this chat room");
        }
        ChatMessage msg = ChatMessage.builder()
                .chatRoom(room).sender(sender).content(content).isRead(false).build();
        msg = chatMessageRepository.save(msg);
        room.setLastMessageAt(LocalDateTime.now());
        chatRoomRepository.save(room);
        return mapMsgToResponse(msg);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long userId, Long roomId) {
        chatMessageRepository.markMessagesAsRead(roomId, userId);
    }

    private ChatRoomResponse mapRoomToResponse(ChatRoom room) {
        String lastMsg = chatMessageRepository.findLastMessageByRoomId(room.getId())
                .map(ChatMessage::getContent).orElse(null);
        return ChatRoomResponse.builder()
                .id(room.getId())
                .userOneId(room.getUserOne().getId())
                .userOneUsername(room.getUserOne().getUsername())
                .userOneAvatar(room.getUserOne().getAvatarUrl())
                .userTwoId(room.getUserTwo().getId())
                .userTwoUsername(room.getUserTwo().getUsername())
                .userTwoAvatar(room.getUserTwo().getAvatarUrl())
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .lastMessage(lastMsg)
                .build();
    }

    private ChatMessageResponse mapMsgToResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId()).roomId(msg.getChatRoom().getId())
                .senderId(msg.getSender().getId())
                .senderUsername(msg.getSender().getUsername())
                .senderAvatar(msg.getSender().getAvatarUrl())
                .content(msg.getContent()).isRead(msg.getIsRead()).sentAt(msg.getSentAt())
                .build();
    }
}
