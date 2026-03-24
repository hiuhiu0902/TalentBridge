package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.UserProfileResponse;
import com.demo.talentbridge.dto.response.UserSearchResponse;
import com.demo.talentbridge.entity.Employer;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.enums.UserRole;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.FollowService;
import com.demo.talentbridge.service.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDirectoryServiceImpl implements UserDirectoryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowService followService;

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResponse> searchUsers(Long currentUserId, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isBlank()) {
            throw new BadRequestException("Search keyword cannot be blank");
        }

        return userRepository.searchUsers(normalizedKeyword).stream()
                .map(user -> mapToSearchResponse(currentUserId, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchResponse> getSuggestions(Long currentUserId) {
        List<User> candidates;

        if (currentUserId == null) {
            candidates = userRepository.findTop20ByActiveTrueOrderByCreatedAtDesc();
        } else {
            candidates = userRepository.findTop50ByActiveTrueAndIdNotOrderByCreatedAtDesc(currentUserId).stream()
                    .filter(user -> !followService.isFollowing(currentUserId, user.getId()))
                    .limit(20)
                    .toList();
        }

        return candidates.stream()
                .map(user -> mapToSearchResponse(currentUserId, user))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long currentUserId, Long targetUserId) {
        User targetUser = userRepository.findActiveProfileById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));

        boolean isAuthenticated = currentUserId != null;
        boolean isMe = isAuthenticated && currentUserId.equals(targetUserId);
        boolean isFollowing = isAuthenticated && !isMe && followService.isFollowing(currentUserId, targetUserId);
        boolean followsYou = isAuthenticated && !isMe && followService.isFollowing(targetUserId, currentUserId);
        boolean isMutualFollow = isFollowing && followsYou;

        return UserProfileResponse.builder()
                .id(targetUser.getId())
                .employerId(targetUser.getEmployer() != null ? targetUser.getEmployer().getId() : null)
                .candidateId(targetUser.getCandidate() != null ? targetUser.getCandidate().getId() : null)
                .username(targetUser.getUsername())
                .fullName(targetUser.getFullName())
                .avatarUrl(targetUser.getAvatarUrl())
                .role(targetUser.getRole())
                .companyName(extractCompanyName(targetUser))
                .subtitle(buildSubtitle(targetUser))
                .followerCount(followService.getFollowerCount(targetUserId))
                .followingCount(followService.getFollowingCount(targetUserId))
                .isFollowing(isFollowing)
                .followsYou(followsYou)
                .isMutualFollow(isMutualFollow)
                .isMe(isMe)
                .canMessage(!isMe && isMutualFollow)
                .build();
    }

    private UserSearchResponse mapToSearchResponse(Long currentUserId, User user) {
        boolean isAuthenticated = currentUserId != null;
        boolean isMe = isAuthenticated && currentUserId.equals(user.getId());
        boolean isFollowing = isAuthenticated && !isMe && followService.isFollowing(currentUserId, user.getId());
        boolean followsYou = isAuthenticated && !isMe && followService.isFollowing(user.getId(), currentUserId);
        boolean isMutualFollow = isFollowing && followsYou;

        return UserSearchResponse.builder()
                .id(user.getId())
                .employerId(user.getEmployer() != null ? user.getEmployer().getId() : null)
                .candidateId(user.getCandidate() != null ? user.getCandidate().getId() : null)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .companyName(extractCompanyName(user))
                .subtitle(buildSubtitle(user))
                .isFollowing(isFollowing)
                .followsYou(followsYou)
                .isMutualFollow(isMutualFollow)
                .canMessage(!isMe && isMutualFollow)
                .isMe(isMe)
                .build();
    }

    private String buildSubtitle(User user) {
        if (user.getRole() == UserRole.EMPLOYER) {
            String companyName = extractCompanyName(user);
            if (companyName != null && !companyName.isBlank()) {
                return companyName;
            }
        }

        return switch (user.getRole()) {
            case CANDIDATE -> "Candidate";
            case EMPLOYER -> "Employer";
            case ADMIN -> "Admin";
        };
    }

    private String extractCompanyName(User user) {
        Employer employer = user.getEmployer();
        if (employer == null) {
            return null;
        }
        return employer.getCompanyName();
    }
}
