package com.demo.talentbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private Long userOneId;
    private String userOneUsername;
    private String userOneAvatar;
    private Long userTwoId;
    private String userTwoUsername;
    private String userTwoAvatar;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private Integer unreadCount;
}
