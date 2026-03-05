package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.FollowResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.FollowService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/connections")
@SecurityRequirement(name = "bearerAuth")

public class ConnectionController {
    @Autowired private FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<FollowResponse>> follow(
            @AuthenticationPrincipal User currentUser, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success("Followed", followService.follow(currentUser.getId(), userId)));
    }

    @DeleteMapping("/{userId}/unfollow")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @AuthenticationPrincipal User currentUser, @PathVariable Long userId) {
        followService.unfollow(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success("Unfollowed", null));
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<List<FollowResponse>>> getFollowers(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.getFollowers(userId)));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<List<FollowResponse>>> getFollowing(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.getFollowing(userId)));
    }

    @GetMapping("/{userId}/is-following")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @AuthenticationPrincipal User currentUser, @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(followService.isFollowing(currentUser.getId(), userId)));
    }
}
