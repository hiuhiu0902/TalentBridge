package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.UserProfileResponse;
import com.demo.talentbridge.dto.response.UserSearchResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.UserDirectoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/directory")
@SecurityRequirement(name = "bearerAuth")
public class UserDirectoryController {

    @Autowired
    private UserDirectoryService userDirectoryService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String keyword) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(userDirectoryService.searchUsers(currentUserId, keyword)));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> getSuggestions(
            @AuthenticationPrincipal User currentUser) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(userDirectoryService.getSuggestions(currentUserId)));
    }

    @GetMapping("/profiles/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long userId) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(ApiResponse.success(userDirectoryService.getPublicProfile(currentUserId, userId)));
    }
}
