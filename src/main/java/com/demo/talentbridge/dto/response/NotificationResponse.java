package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private String referenceUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
