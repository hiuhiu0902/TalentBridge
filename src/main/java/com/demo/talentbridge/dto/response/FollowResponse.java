package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {
    private Long id;
    private Long followerId;
    private String followerUsername;
    private String followerFullName;
    private String followerAvatarUrl;
    private UserRole followerRole;
    private String followerSubtitle;

    private Long followedId;
    private String followedUsername;
    private String followedFullName;
    private LocalDateTime followedAt;
    private String followedAvatarUrl;
    private UserRole followedRole;
    private String followedSubtitle;

    private Long targetUserId;
    private String targetUsername;
    private String targetFullName;
    private String targetAvatarUrl;
    private UserRole targetRole;
    private String targetSubtitle;

    private Boolean isFollowing;
    private Boolean followsYou;
    private Boolean isMutualFollow;
    private Boolean canMessage;
}
