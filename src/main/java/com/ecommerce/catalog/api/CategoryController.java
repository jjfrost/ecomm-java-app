package com.ecommerce.catalog.api;

import com.ecommerce.catalog.api.dto.CategoryRequest;
import com.ecommerce.catalog.api.dto.CategoryResponse;
import com.ecommerce.catalog.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/catalog/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.list();
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse get(@PathVariable @Positive Long categoryId) {
        return categoryService.get(categoryId);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity
                .created(URI.create("/api/catalog/categories/" + response.id()))
                .body(response);
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse update(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long categoryId) {
        categoryService.delete(categoryId);
    }
}
