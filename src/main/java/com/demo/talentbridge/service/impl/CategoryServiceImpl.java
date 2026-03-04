package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.CategoryRequest;
import com.demo.talentbridge.dto.response.CategoryResponse;
import com.demo.talentbridge.entity.Category;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.CategoryRepository;
import com.demo.talentbridge.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired private CategoryRepository categoryRepository;

    @Override @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName()))
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        Category c = Category.builder().name(request.getName()).description(request.getDescription()).build();
        return map(categoryRepository.save(c));
    }

    @Override @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category c = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        c.setName(request.getName());
        c.setDescription(request.getDescription());
        return map(categoryRepository.save(c));
    }

    @Override @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category","id",id));
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return map(categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category","id",id)));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    private CategoryResponse map(Category c) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).description(c.getDescription()).build();
    }
}
