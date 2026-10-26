package com.ecommerce.catalog.api;

import com.ecommerce.catalog.api.dto.ProductAttributeRequest;
import com.ecommerce.catalog.api.dto.ProductAttributeResponse;
import com.ecommerce.catalog.service.ProductService;
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
@RequestMapping("/api/catalog/attributes")
public class ProductAttributeController {
    private final ProductService productService;

    public ProductAttributeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductAttributeResponse> list() {
        return productService.listAttributeDefinitions();
    }

    @PostMapping
    public ResponseEntity<ProductAttributeResponse> create(
            @Valid @RequestBody ProductAttributeRequest request) {
        ProductAttributeResponse response = productService.createAttribute(request);
        return ResponseEntity
                .created(URI.create("/api/catalog/attributes/" + response.id()))
                .body(response);
    }

    @PutMapping("/{attributeId}")
    public ProductAttributeResponse update(
            @PathVariable @Positive Long attributeId,
            @Valid @RequestBody ProductAttributeRequest request) {
        return productService.updateAttribute(attributeId, request);
    }

    @DeleteMapping("/{attributeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long attributeId) {
        productService.deleteAttribute(attributeId);
    }
}
