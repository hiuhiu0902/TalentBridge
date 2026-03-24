package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.response.FollowResponse;
import com.demo.talentbridge.entity.FollowConnection;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.exception.BadRequestException;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.FollowConnectionRepository;
import com.demo.talentbridge.repository.UserRepository;
import com.demo.talentbridge.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    @Autowired
    private FollowConnectionRepository followConnectionRepository;

    @Autowired
    private UserRepository userRepository;

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
        return mapToResponse(connection);
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
    public List<FollowResponse> getFollowers(Long userId) {
        validateUserExists(userId);
        return followConnectionRepository.findByFollowedIdOrderByFollowedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> getFollowing(Long userId) {
        validateUserExists(userId);
        return followConnectionRepository.findByFollowerIdOrderByFollowedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerUserId, Long followedUserId) {
        return followConnectionRepository.existsByFollowerIdAndFollowedId(followerUserId, followedUserId);
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

    private FollowResponse mapToResponse(FollowConnection fc) {
        return FollowResponse.builder()
                .id(fc.getId())
                .followerId(fc.getFollower().getId())
                .followerUsername(fc.getFollower().getUsername())
                .followerAvatarUrl(fc.getFollower().getAvatarUrl())
                .followedId(fc.getFollowed().getId())
                .followedUsername(fc.getFollowed().getUsername())
                .followedAvatarUrl(fc.getFollowed().getAvatarUrl())
                .followedAt(fc.getFollowedAt())
                .build();
    }
}