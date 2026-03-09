package com.demo.talentbridge.dto.response;

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
    private String followerAvatarUrl;

    private Long followedId;
    private String followedUsername;
    private LocalDateTime followedAt;
    private String followedAvatarUrl;
}
