package com.demo.talentbridge.controller;

import com.cloudinary.http44.api.Response;
import com.demo.talentbridge.dto.request.CategoryRequest;
import com.demo.talentbridge.dto.response.ApiResponse;
import com.demo.talentbridge.dto.response.CategoryResponse;
import com.demo.talentbridge.service.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public read-only category endpoints.
 * Admin CRUD for categories is handled in AdminController.
 */
@RestController
@RequestMapping("/api/v1/categories")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize(" hasRole('ADMIN')") // Allow both USER and ADMIN to access these endpoints
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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.createCategory(categoryRequest)));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Delete category successfully"));
    }
}
