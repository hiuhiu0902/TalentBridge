package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.NotificationResponse;
import com.demo.talentbridge.entity.Notification;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.NotificationType;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.exception.UnauthorizedException;
import com.demo.talentbridge.repository.FollowConnectionRepository;
import com.demo.talentbridge.repository.NotificationRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowConnectionRepository followConnectionRepository;

    @Override
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to update this notification");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this notification");
        }
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void createNotification(User user, String title, String content, NotificationType type, String referenceUrl) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(type)
                .referenceUrl(referenceUrl)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyFollowersOfNewJob(Long employerUserId, Long jobPostId, String jobTitle) {
        // Get all follower IDs of this employer
        List<Long> followerIds = followConnectionRepository.findFollowerIdsByFollowedId(employerUserId);

        for (Long followerId : followerIds) {
            User follower = userRepository.findById(followerId).orElse(null);
            if (follower != null) {
                createNotification(
                        follower,
                        "New Job Posted",
                        "A company you follow posted a new job: " + jobTitle,
                        NotificationType.NEW_JOB_FROM_FOLLOWED_EMPLOYER,
                        "/jobs/" + jobPostId
                );
            }
        }
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .referenceUrl(n.getReferenceUrl())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
