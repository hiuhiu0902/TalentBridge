package com.demo.talentbridge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileResponse {
    private Long id;
    private String companyName;
    private String website;
    private String description;
    private String logoUrl;
    private String industry;
    private String companySize;
    private String address;
    private String email;
    private Long followerCount;
}
