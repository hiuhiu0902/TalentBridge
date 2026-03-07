package com.demo.talentbridge.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class CandidateProfileRequest {

    @Size(max = 100)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    private String summary;


    private MultipartFile cvUrl;


    private MultipartFile avatarUrl;
}
