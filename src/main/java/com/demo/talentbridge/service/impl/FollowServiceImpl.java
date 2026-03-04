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
    public List<FollowResponse> getFollowers(Long userId) {
        return followConnectionRepository.findByFollowedId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowResponse> getFollowing(Long userId) {
        return followConnectionRepository.findByFollowerId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFollowing(Long followerUserId, Long followedUserId) {
        return followConnectionRepository.existsByFollowerIdAndFollowedId(followerUserId, followedUserId);
    }

    @Override
    public long getFollowerCount(Long userId) {
        return followConnectionRepository.countByFollowedId(userId);
    }

    private FollowResponse mapToResponse(FollowConnection fc) {
        return FollowResponse.builder()
                .id(fc.getId())
                .followerId(fc.getFollower().getId())
                .followerUsername(fc.getFollower().getUsername())
                .followedId(fc.getFollowed().getId())
                .followedUsername(fc.getFollowed().getUsername())
                .followedAt(fc.getFollowedAt())
                .build();
    }
}
