package com.demo.talentbridge.dto.response;

import com.demo.talentbridge.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private Long employerId;
    private Long candidateId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private UserRole role;

    private String companyName;
    private String subtitle;

    private Long followerCount;
    private Long followingCount;

    private Boolean isFollowing;
    private Boolean followsYou;
    private Boolean isMutualFollow;
    private Boolean isMe;
    private Boolean canMessage;
}
