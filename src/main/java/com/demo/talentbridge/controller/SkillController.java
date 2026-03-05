package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.enums.SkillName;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Skills are now enum-based. This controller exposes the list of all available skills.
 * No CRUD needed — skills are managed via the SkillName enum.
 */
@RestController
@RequestMapping("/api/v1/skills")
@SecurityRequirement(name = "bearerAuth")

public class SkillController {

    /**
     * Returns all available skill names from the enum.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAllSkills() {
        List<SkillResponse> skills = Arrays.stream(SkillName.values())
                .map(s -> SkillResponse.builder()
                        .skillName(s)
                        .displayName(s.name())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(skills));
    }

    /**
     * Returns skill names grouped by category for frontend dropdowns.
     */
    @GetMapping("/names")
    public ResponseEntity<ApiResponse<List<String>>> getAllSkillNames() {
        List<String> names = Arrays.stream(SkillName.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(names));
    }
}
