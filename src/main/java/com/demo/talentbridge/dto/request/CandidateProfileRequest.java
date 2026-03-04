package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CandidateProfileRequest {

    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    private String summary;

    @Size(max = 255)
    private String cvUrl;

    @Size(max = 255)
    private String avatarUrl;
}
