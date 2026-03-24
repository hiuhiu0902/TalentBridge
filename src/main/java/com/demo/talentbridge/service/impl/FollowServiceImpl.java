package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.FollowResponse;
import com.demo.talentbridge.entity.Employer;
import com.demo.talentbridge.entity.FollowConnection;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.NotificationType;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.FollowConnectionRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.FollowService;
import com.demo.talentbridge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowConnectionRepository followConnectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public FollowResponse follow(Long followerUserId, Long followedUserId) {
        if (followerUserId.equals(followedUserId)) {
            throw new BadRequestException("You cannot follow yourself");
        }

        User follower = userRepository.findById(followerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followerUserId));
        User followed = userRepository.findById(followedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", followedUserId));

        if (followConnectionRepository.existsByFollowerIdAndFollowedId(followerUserId, followedUserId)) {
            throw new DuplicateResourceException("You are already following this user");
        }

        FollowConnection connection = FollowConnection.builder()
                .follower(follower)
                .followed(followed)
                .build();

        connection = followConnectionRepository.save(connection);

        notificationService.createNotification(
                followed,
                "New follower",
                displayName(follower) + " started following you.",
                NotificationType.FOLLOWED_YOU,
                "/directory/profiles/" + follower.getId()
        );

        return mapFollowingResponse(connection, followerUserId);
    }

    @Override
    @Transactional
    public void unfollow(Long followerUserId, Long followedUserId) {
        FollowConnection connection = followConnectionRepository
                .findByFollowerIdAndFollowedId(followerUserId, followedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow connection not found"));
        followConnectionRepository.delete(connection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowers(Long currentUserId, Long userId) {
        validateUserExists(userId);
        return followConnectionRepository.findByFollowedIdOrderByFollowedAtDesc(userId).stream()
                .map(fc -> mapFollowerResponse(fc, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowing(Long currentUserId, Long userId) {
        validateUserExists(userId);
        return followConnectionRepository.findByFollowerIdOrderByFollowedAtDesc(userId).stream()
                .map(fc -> mapFollowingResponse(fc, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerUserId, Long followedUserId) {
        if (followerUserId == null || followedUserId == null) {
            return false;
        }
        return followConnectionRepository.existsByFollowerIdAndFollowedId(followerUserId, followedUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMutualFollow(Long firstUserId, Long secondUserId) {
        if (firstUserId == null || secondUserId == null || firstUserId.equals(secondUserId)) {
            return false;
        }
        return isFollowing(firstUserId, secondUserId) && isFollowing(secondUserId, firstUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowerCount(Long userId) {
        validateUserExists(userId);
        return followConnectionRepository.countByFollowedId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        validateUserExists(userId);
        return followConnectionRepository.countByFollowerId(userId);
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
    }

    private FollowResponse mapFollowerResponse(FollowConnection fc, Long currentUserId) {
        return mapToResponse(fc, currentUserId, fc.getFollower());
    }

    private FollowResponse mapFollowingResponse(FollowConnection fc, Long currentUserId) {
        return mapToResponse(fc, currentUserId, fc.getFollowed());
    }

    private FollowResponse mapToResponse(FollowConnection fc, Long currentUserId, User targetUser) {
        boolean isAuthenticated = currentUserId != null;
        boolean isMe = isAuthenticated && currentUserId.equals(targetUser.getId());
        boolean isFollowing = isAuthenticated && !isMe && isFollowing(currentUserId, targetUser.getId());
        boolean followsYou = isAuthenticated && !isMe && isFollowing(targetUser.getId(), currentUserId);
        boolean isMutualFollow = isFollowing && followsYou;

        return FollowResponse.builder()
                .id(fc.getId())
                .followerId(fc.getFollower().getId())
                .followerUsername(fc.getFollower().getUsername())
                .followerFullName(fc.getFollower().getFullName())
                .followerAvatarUrl(fc.getFollower().getAvatarUrl())
                .followerRole(fc.getFollower().getRole())
                .followerSubtitle(buildSubtitle(fc.getFollower()))
                .followedId(fc.getFollowed().getId())
                .followedUsername(fc.getFollowed().getUsername())
                .followedFullName(fc.getFollowed().getFullName())
                .followedAvatarUrl(fc.getFollowed().getAvatarUrl())
                .followedRole(fc.getFollowed().getRole())
                .followedSubtitle(buildSubtitle(fc.getFollowed()))
                .followedAt(fc.getFollowedAt())
                .targetUserId(targetUser.getId())
                .targetUsername(targetUser.getUsername())
                .targetFullName(targetUser.getFullName())
                .targetAvatarUrl(targetUser.getAvatarUrl())
                .targetRole(targetUser.getRole())
                .targetSubtitle(buildSubtitle(targetUser))
                .isFollowing(isFollowing)
                .followsYou(followsYou)
                .isMutualFollow(isMutualFollow)
                .canMessage(!isMe && isMutualFollow)
                .build();
    }

    private String buildSubtitle(User user) {
        if (user == null) {
            return null;
        }

        if (user.getRole() == UserRole.EMPLOYER) {
            Employer employer = user.getEmployer();
            if (employer != null && employer.getCompanyName() != null && !employer.getCompanyName().isBlank()) {
                return employer.getCompanyName();
            }
        }

        return switch (user.getRole()) {
            case CANDIDATE -> "Candidate";
            case EMPLOYER -> "Employer";
            case ADMIN -> "Admin";
        };
    }

    private String displayName(User user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        return user.getUsername();
    }
}
