package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.SkillRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {
    @Autowired private SkillService skillService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(skillService.getAllSkills()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkillById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SkillResponse>> create(@Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.createSkill(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> update(@PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success(skillService.updateSkill(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        skillService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }
}
