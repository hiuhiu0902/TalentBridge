package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.FollowResponse;

import java.util.List;

public interface FollowService {
    FollowResponse follow(Long followerUserId, Long followedUserId);
    void unfollow(Long followerUserId, Long followedUserId);
    List<FollowResponse> getFollowers(Long currentUserId, Long userId);
    List<FollowResponse> getFollowing(Long currentUserId, Long userId);
    boolean isFollowing(Long followerUserId, Long followedUserId);
    boolean isMutualFollow(Long firstUserId, Long secondUserId);
    long getFollowerCount(Long userId);
    long getFollowingCount(Long userId);
}
