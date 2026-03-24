package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.ChatMessageResponse;
import com.demo.talentbridge.dto.response.ChatRoomResponse;
import com.demo.talentbridge.entity.ChatMessage;
import com.demo.talentbridge.entity.ChatRoom;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.ChatMessageRepository;
import com.demo.talentbridge.repository.ChatRoomRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.ChatService;
import com.demo.talentbridge.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private static final int MAX_MESSAGE_LENGTH = 4000;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowService followService;

    @Override
    @Transactional
    public ChatRoomResponse getOrCreateRoom(Long userOneId, Long userTwoId) {
        if (userOneId == null || userTwoId == null) {
            throw new BadRequestException("User ids are required");
        }

        if (userOneId.equals(userTwoId)) {
            throw new BadRequestException("You cannot create a chat room with yourself");
        }

        Long firstUserId = Math.min(userOneId, userTwoId);
        Long secondUserId = Math.max(userOneId, userTwoId);

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByUsers(firstUserId, secondUserId);
        if (existingRoom.isPresent()) {
            return mapRoomToResponse(existingRoom.get(), userOneId);
        }

        ensureMutualFollow(userOneId, userTwoId);

        User firstUser = userRepository.findById(firstUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", firstUserId));
        User secondUser = userRepository.findById(secondUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", secondUserId));

        ChatRoom room = ChatRoom.builder()
                .userOne(firstUser)
                .userTwo(secondUser)
                .createdAt(LocalDateTime.now())
                .lastMessageAt(null)
                .build();

        return mapRoomToResponse(chatRoomRepository.save(room), userOneId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRoomsForUser(Long userId) {
        requireUser(userId);

        return chatRoomRepository.findByUserId(userId).stream()
                .map(room -> mapRoomToResponse(room, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long userId, Long roomId) {
        ChatRoom room = requireRoomMember(userId, roomId);

        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(room.getId()).stream()
                .map(this::mapMsgToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, Long roomId, String content) {
        ChatRoom room = requireRoomMember(senderId, roomId);
        User sender = requireUser(senderId);
        Long recipientId = resolveOtherUserId(room, senderId);

        ensureMutualFollow(senderId, recipientId);

        String normalizedContent = normalizeMessageContent(content);

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(normalizedContent)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        message = chatMessageRepository.save(message);

        room.setLastMessageAt(message.getSentAt());
        chatRoomRepository.save(room);

        return mapMsgToResponse(message);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long userId, Long roomId) {
        ChatRoom room = requireRoomMember(userId, roomId);
        chatMessageRepository.markMessagesAsRead(room.getId(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long resolveRecipientId(Long userId, Long roomId) {
        ChatRoom room = requireRoomMember(userId, roomId);
        return resolveOtherUserId(room, userId);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ChatRoom requireRoomMember(Long userId, Long roomId) {
        if (roomId == null) {
            throw new BadRequestException("Room id is required");
        }

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("ChatRoom", "id", roomId));

        boolean isMember = room.getUserOne().getId().equals(userId) || room.getUserTwo().getId().equals(userId);
        if (!isMember) {
            throw new UnauthorizedException("You are not a member of this chat room");
        }

        return room;
    }

    private void ensureMutualFollow(Long userOneId, Long userTwoId) {
        if (!followService.isMutualFollow(userOneId, userTwoId)) {
            throw new BadRequestException("Only users who follow each other can chat");
        }
    }

    private Long resolveOtherUserId(ChatRoom room, Long currentUserId) {
        return room.getUserOne().getId().equals(currentUserId)
                ? room.getUserTwo().getId()
                : room.getUserOne().getId();
    }

    private String normalizeMessageContent(String content) {
        if (content == null) {
            throw new BadRequestException("Message content is required");
        }

        String normalized = content.trim();
        if (normalized.isBlank()) {
            throw new BadRequestException("Message content is required");
        }

        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            throw new BadRequestException("Message content must not exceed " + MAX_MESSAGE_LENGTH + " characters");
        }

        return normalized;
    }

    private ChatRoomResponse mapRoomToResponse(ChatRoom room, Long currentUserId) {
        String lastMessage = chatMessageRepository.findTopByChatRoomIdOrderBySentAtDesc(room.getId())
                .map(ChatMessage::getContent)
                .orElse(null);

        int unreadCount = currentUserId == null
                ? 0
                : Math.toIntExact(chatMessageRepository.countByChatRoomIdAndIsReadFalseAndSenderIdNot(room.getId(), currentUserId));

        User otherUser = null;
        boolean isMutualFollow = false;
        if (currentUserId != null) {
            otherUser = room.getUserOne().getId().equals(currentUserId) ? room.getUserTwo() : room.getUserOne();
            isMutualFollow = followService.isMutualFollow(currentUserId, otherUser.getId());
        }

        return ChatRoomResponse.builder()
                .id(room.getId())
                .userOneId(room.getUserOne().getId())
                .userOneUsername(room.getUserOne().getUsername())
                .userOneFullName(room.getUserOne().getFullName())
                .userOneAvatar(room.getUserOne().getAvatarUrl())
                .userTwoId(room.getUserTwo().getId())
                .userTwoUsername(room.getUserTwo().getUsername())
                .userTwoFullName(room.getUserTwo().getFullName())
                .userTwoAvatar(room.getUserTwo().getAvatarUrl())
                .otherUserId(otherUser != null ? otherUser.getId() : null)
                .otherUsername(otherUser != null ? otherUser.getUsername() : null)
                .otherFullName(otherUser != null ? otherUser.getFullName() : null)
                .otherAvatar(otherUser != null ? otherUser.getAvatarUrl() : null)
                .isMutualFollow(isMutualFollow)
                .canMessage(otherUser != null && isMutualFollow)
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .lastMessage(lastMessage)
                .unreadCount(unreadCount)
                .build();
    }

    private ChatMessageResponse mapMsgToResponse(ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .roomId(msg.getChatRoom().getId())
                .senderId(msg.getSender().getId())
                .senderUsername(msg.getSender().getUsername())
                .senderAvatar(msg.getSender().getAvatarUrl())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .sentAt(msg.getSentAt())
                .build();
    }
}
