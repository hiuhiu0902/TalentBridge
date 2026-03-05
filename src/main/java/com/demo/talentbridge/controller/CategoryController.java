package com.demo.talentbridge.controller;

import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.CategoryResponse;
import com.demo.talentbridge.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public read-only category endpoints.
 * Admin CRUD for categories is handled in AdminController.
 */
@RestController
@RequestMapping("/api/v1/categories")
@SecurityRequirement(name = "bearerAuth")

public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id)));
    }
}
