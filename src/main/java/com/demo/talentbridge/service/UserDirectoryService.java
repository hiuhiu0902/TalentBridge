package com.demo.talentbridge.service;

import com.demo.talentbridge.dto.response.UserProfileResponse;
import com.demo.talentbridge.dto.response.UserSearchResponse;

import java.util.List;

public interface UserDirectoryService {
    List<UserSearchResponse> searchUsers(Long currentUserId, String keyword);
    List<UserSearchResponse> getSuggestions(Long currentUserId);
    UserProfileResponse getPublicProfile(Long currentUserId, Long targetUserId);
}