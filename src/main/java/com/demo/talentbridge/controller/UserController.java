package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.request.UpdatePasswordRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.UserResponse;
import com.demo.talentbridge.entity.User;
import com.demo.talentbridge.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password updated", null));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@AuthenticationPrincipal User currentUser) {
        userService.deactivateAccount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deactivated", null));
    }
}
