package com.demo.talentbridge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployerProfileRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String companyName;

    @Size(max = 255)
    private String website;

    private String description;

    @Size(max = 255)
    private String logoUrl;

    @Size(max = 100)
    private String industry;

    @Size(max = 100)
    private String companySize;

    @Size(max = 255)
    private String address;
}
