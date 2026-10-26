package com.ecommerce.catalog.service;

import com.ecommerce.catalog.api.dto.CategoryRequest;
import com.ecommerce.catalog.api.dto.CategoryResponse;
import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.mapper.CatalogMapper;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CatalogMapper catalogMapper;

    public CategoryService(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            CatalogMapper catalogMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categoryRepository.findAllByOrderByNameAsc()
                .stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long categoryId) {
        return catalogMapper.toResponse(findCategory(categoryId));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException(
                    "CATEGORY_NAME_IN_USE",
                    "A category named " + name + " already exists");
        }
        return catalogMapper.toResponse(categoryRepository.save(new Category(name)));
    }

    @Transactional
    public CategoryResponse update(Long categoryId, CategoryRequest request) {
        Category category = findCategory(categoryId);
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, categoryId)) {
            throw new BusinessRuleException(
                    "CATEGORY_NAME_IN_USE",
                    "A category named " + name + " already exists");
        }
        category.rename(name);
        return catalogMapper.toResponse(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        Category category = findCategory(categoryId);
        long productCount = productRepository.countByCategoryId(categoryId);
        if (productCount > 0) {
            throw new BusinessRuleException(
                    "CATEGORY_IN_USE",
                    "Category "
                            + categoryId
                            + " cannot be deleted because it contains "
                            + productCount
                            + " products");
        }
        categoryRepository.delete(category);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }
}
