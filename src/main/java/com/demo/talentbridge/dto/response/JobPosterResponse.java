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
public class JobPosterResponse {
    private Long userId;
    private Long employerId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String companyName;
    private String companyLogoUrl;
    private UserRole role;
    private Boolean isFollowing;
    private Boolean followsYou;
    private Boolean isMutualFollow;
    private Boolean canMessage;
    private Boolean isMe;
}
