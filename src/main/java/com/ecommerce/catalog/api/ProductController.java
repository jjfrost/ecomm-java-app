package com.ecommerce.catalog.api;

import com.ecommerce.catalog.api.dto.ProductAttributeValueRequest;
import com.ecommerce.catalog.api.dto.ProductAttributeValueResponse;
import com.ecommerce.catalog.api.dto.ProductCreateRequest;
import com.ecommerce.catalog.api.dto.ProductDetailResponse;
import com.ecommerce.catalog.api.dto.ProductImageRequest;
import com.ecommerce.catalog.api.dto.ProductImageResponse;
import com.ecommerce.catalog.api.dto.ProductSummaryResponse;
import com.ecommerce.catalog.api.dto.ProductUpdateRequest;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.api.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/catalog/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public PageResponse<ProductSummaryResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) @Positive Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return productService.search(
                q,
                categoryId,
                active,
                minPrice,
                maxPrice,
                pageable);
    }

    @GetMapping("/{productId}")
    public ProductDetailResponse get(@PathVariable @Positive Long productId) {
        return productService.get(productId);
    }

    @PostMapping
    public ResponseEntity<ProductDetailResponse> create(
            @Valid @RequestBody ProductCreateRequest request) {
        ProductDetailResponse response = productService.create(request);
        return ResponseEntity
                .created(URI.create("/api/catalog/products/" + response.id()))
                .body(response);
    }

    @PutMapping("/{productId}")
    public ProductDetailResponse update(
            @PathVariable @Positive Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(productId, request);
    }

    @PostMapping("/{productId}/activation")
    public ProductDetailResponse activate(@PathVariable @Positive Long productId) {
        return productService.activate(productId);
    }

    @DeleteMapping("/{productId}/activation")
    public ProductDetailResponse deactivate(@PathVariable @Positive Long productId) {
        return productService.deactivate(productId);
    }

    @GetMapping("/{productId}/images")
    public List<ProductImageResponse> listImages(
            @PathVariable @Positive Long productId) {
        return productService.listImages(productId);
    }

    @PostMapping("/{productId}/images")
    public ResponseEntity<ProductImageResponse> addImage(
            @PathVariable @Positive Long productId,
            @Valid @RequestBody ProductImageRequest request) {
        ProductImageResponse response = productService.addImage(productId, request);
        return ResponseEntity
                .created(URI.create(
                        "/api/catalog/products/" + productId + "/images/" + response.id()))
                .body(response);
    }

    @PutMapping("/{productId}/images/{imageId}")
    public ProductImageResponse updateImage(
            @PathVariable @Positive Long productId,
            @PathVariable @Positive Long imageId,
            @Valid @RequestBody ProductImageRequest request) {
        return productService.updateImage(productId, imageId, request);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(
            @PathVariable @Positive Long productId,
            @PathVariable @Positive Long imageId) {
        productService.deleteImage(productId, imageId);
    }

    @GetMapping("/{productId}/attributes")
    public List<ProductAttributeValueResponse> listAttributeValues(
            @PathVariable @Positive Long productId) {
        return productService.listAttributeValues(productId);
    }

    @PutMapping("/{productId}/attributes/{attributeId}")
    public ProductAttributeValueResponse setAttributeValue(
            @PathVariable @Positive Long productId,
            @PathVariable @Positive Long attributeId,
            @Valid @RequestBody ProductAttributeValueRequest request) {
        return productService.setAttributeValue(productId, attributeId, request);
    }

    @DeleteMapping("/{productId}/attributes/{attributeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAttributeValue(
            @PathVariable @Positive Long productId,
            @PathVariable @Positive Long attributeId) {
        productService.deleteAttributeValue(productId, attributeId);
    }
}
