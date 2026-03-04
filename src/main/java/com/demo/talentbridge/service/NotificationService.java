package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.NotificationResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.NotificationType;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getNotifications(Long userId);
    List<NotificationResponse> getUnreadNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long userId, Long notificationId);
    void createNotification(User user, String title, String content, NotificationType type, String referenceUrl);
    void notifyFollowersOfNewJob(Long employerUserId, Long jobPostId, String jobTitle);
}
