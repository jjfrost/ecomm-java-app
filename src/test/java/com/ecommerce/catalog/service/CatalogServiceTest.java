package com.ecommerce.catalog.service;

import com.ecommerce.catalog.api.dto.ProductBundleItemRequest;
import com.ecommerce.catalog.api.dto.ProductBundleRequest;
import com.ecommerce.catalog.api.dto.ProductBundleResponse;
import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.ProductBundle;
import com.ecommerce.catalog.domain.ProductBundleItem;
import com.ecommerce.catalog.mapper.BundleMapper;
import com.ecommerce.catalog.mapper.CatalogMapper;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.ecommerce.catalog.repository.ProductBundleItemRepository;
import com.ecommerce.catalog.repository.ProductBundleRepository;
import com.ecommerce.catalog.repository.ProductImageRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.common.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductImageRepository imageRepository;

    @Mock
    private ProductAttributeRepository attributeRepository;

    @Mock
    private ProductAttributeValueRepository valueRepository;

    @Mock
    private ProductBundleRepository bundleRepository;

    @Mock
    private ProductBundleItemRepository bundleItemRepository;

    private ProductService productService;
    private BundleService bundleService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(
                productRepository,
                categoryRepository,
                imageRepository,
                attributeRepository,
                valueRepository,
                bundleRepository,
                bundleItemRepository,
                new CatalogMapper());
        bundleService = new BundleService(
                bundleRepository,
                bundleItemRepository,
                productRepository,
                new BundleMapper());
    }

    @Test
    void activationRejectsProductWithoutPrimaryImage() {
        Product product = product(44L, "SKU-44", "Keyboard", "49.95", 3L);
        when(productRepository.findById(44L)).thenReturn(Optional.of(product));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(new Category("Hardware")));
        when(imageRepository.countByProductId(44L)).thenReturn(0L);

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> productService.activate(44L));

        assertEquals("PRODUCT_IMAGE_REQUIRED", exception.getRuleCode());
        assertFalse(product.isActive());
        verify(imageRepository, never()).findFirstByProductIdAndPrimaryImageTrue(44L);
    }

    @Test
    void bundleCreationPersistsDiscountedCompositionAsDraft() {
        Product keyboard = product(1L, "KEY-1", "Keyboard", "40.00", 3L);
        Product mouse = product(2L, "MOUSE-1", "Mouse", "30.00", 3L);
        keyboard.activate();
        mouse.activate();
        ProductBundleRequest request = new ProductBundleRequest(
                "Desk Set",
                "Keyboard and two mice",
                new BigDecimal("90.00"),
                List.of(
                        new ProductBundleItemRequest(1L, 1),
                        new ProductBundleItemRequest(2L, 2)));
        when(bundleRepository.existsByNameIgnoreCase("Desk Set")).thenReturn(false);
        when(productRepository.findAllById(any())).thenReturn(List.of(keyboard, mouse));
        when(bundleRepository.saveAndFlush(any(ProductBundle.class)))
                .thenAnswer(invocation -> {
                    ProductBundle bundle = invocation.getArgument(0);
                    ReflectionTestUtils.setField(bundle, "id", 20L);
                    return bundle;
                });
        when(bundleItemRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductBundleResponse response = bundleService.create(request);

        assertEquals(new BigDecimal("100.00"), response.componentTotal());
        assertEquals(new BigDecimal("10.00"), response.savings());
        assertEquals(2, response.items().size());
        assertFalse(response.active());
        verify(bundleItemRepository).saveAll(anyList());
    }

    private static Product product(
            Long id,
            String sku,
            String name,
            String price,
            Long categoryId) {
        Product product = new Product(
                sku,
                name,
                null,
                new BigDecimal(price),
                categoryId);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
