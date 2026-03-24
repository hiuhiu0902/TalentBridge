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
    private String userOneFullName;
    private String userOneAvatar;
    private Long userTwoId;
    private String userTwoUsername;
    private String userTwoFullName;
    private String userTwoAvatar;
    private Long otherUserId;
    private String otherUsername;
    private String otherFullName;
    private String otherAvatar;
    private Boolean isMutualFollow;
    private Boolean canMessage;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private String lastMessage;
    private Integer unreadCount;
}
