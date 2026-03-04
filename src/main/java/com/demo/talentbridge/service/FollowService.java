package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.FollowResponse;

import java.util.List;

public interface FollowService {
    FollowResponse follow(Long followerUserId, Long followedUserId);
    void unfollow(Long followerUserId, Long followedUserId);
    List<FollowResponse> getFollowers(Long userId);
    List<FollowResponse> getFollowing(Long userId);
    boolean isFollowing(Long followerUserId, Long followedUserId);
    long getFollowerCount(Long userId);
}
