package com.demo.talentbridge.dto.request;

import com.demo.talentbridge.enums.UserRole;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    private String idToken;

    private UserRole role;

    private String companyName;
}
