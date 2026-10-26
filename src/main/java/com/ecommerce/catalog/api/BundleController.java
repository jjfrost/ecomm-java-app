package com.ecommerce.catalog.api;

import com.ecommerce.catalog.api.dto.ProductBundleRequest;
import com.ecommerce.catalog.api.dto.ProductBundleResponse;
import com.ecommerce.catalog.service.BundleService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/catalog/bundles")
public class BundleController {
    private final BundleService bundleService;

    public BundleController(BundleService bundleService) {
        this.bundleService = bundleService;
    }

    @GetMapping
    public PageResponse<ProductBundleResponse> list(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return bundleService.list(pageable);
    }

    @GetMapping("/{bundleId}")
    public ProductBundleResponse get(@PathVariable @Positive Long bundleId) {
        return bundleService.get(bundleId);
    }

    @PostMapping
    public ResponseEntity<ProductBundleResponse> create(
            @Valid @RequestBody ProductBundleRequest request) {
        ProductBundleResponse response = bundleService.create(request);
        return ResponseEntity
                .created(URI.create("/api/catalog/bundles/" + response.id()))
                .body(response);
    }

    @PutMapping("/{bundleId}")
    public ProductBundleResponse update(
            @PathVariable @Positive Long bundleId,
            @Valid @RequestBody ProductBundleRequest request) {
        return bundleService.update(bundleId, request);
    }

    @PostMapping("/{bundleId}/activation")
    public ProductBundleResponse activate(@PathVariable @Positive Long bundleId) {
        return bundleService.activate(bundleId);
    }

    @DeleteMapping("/{bundleId}/activation")
    public ProductBundleResponse deactivate(@PathVariable @Positive Long bundleId) {
        return bundleService.deactivate(bundleId);
    }

    @DeleteMapping("/{bundleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long bundleId) {
        bundleService.delete(bundleId);
    }
}
