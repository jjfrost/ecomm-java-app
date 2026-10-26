package com.ecommerce.supplier.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.supplier.dto.SupplierDtos.ChangeSupplierStatusRequest;
import com.ecommerce.supplier.dto.SupplierDtos.CreateSupplierRequest;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierProductRequest;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierProductResponse;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierResponse;
import com.ecommerce.supplier.dto.SupplierDtos.UpdateSupplierProductRequest;
import com.ecommerce.supplier.dto.SupplierDtos.UpdateSupplierRequest;
import com.ecommerce.supplier.model.SupplierStatus;
import com.ecommerce.supplier.service.SupplierCatalogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/suppliers")
@PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
public class SupplierController {
    private final SupplierCatalogService supplierService;

    public SupplierController(SupplierCatalogService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/{supplierId}")
    public SupplierResponse getSupplier(@PathVariable @Positive Long supplierId) {
        return supplierService.getSupplier(supplierId);
    }

    @GetMapping
    public PageResponse<SupplierResponse> findSuppliers(
            @RequestParam(required = false) SupplierStatus status,
            @RequestParam(required = false) @Size(max = 200) String query,
            @PageableDefault(size = 25, sort = "supplierName") Pageable pageable) {
        return supplierService.findSuppliers(status, query, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(request));
    }

    @PutMapping("/{supplierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse updateSupplier(
            @PathVariable @Positive Long supplierId,
            @Valid @RequestBody UpdateSupplierRequest request) {
        return supplierService.updateSupplier(supplierId, request);
    }

    @PatchMapping("/{supplierId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierResponse changeStatus(
            @PathVariable @Positive Long supplierId,
            @Valid @RequestBody ChangeSupplierStatusRequest request) {
        return supplierService.changeStatus(supplierId, request);
    }

    @PostMapping("/{supplierId}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierProductResponse> addProduct(
            @PathVariable @Positive Long supplierId,
            @Valid @RequestBody SupplierProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.addProduct(supplierId, request));
    }

    @PutMapping("/{supplierId}/products/{supplierProductId}")
    @PreAuthorize("hasRole('ADMIN')")
    public SupplierProductResponse updateProduct(
            @PathVariable @Positive Long supplierId,
            @PathVariable @Positive Long supplierProductId,
            @Valid @RequestBody UpdateSupplierProductRequest request) {
        return supplierService.updateProduct(supplierId, supplierProductId, request);
    }

    @GetMapping("/products")
    public PageResponse<SupplierProductResponse> findProducts(
            @RequestParam(required = false) @Positive Long supplierId,
            @RequestParam(required = false) @Positive Long productId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 25, sort = "supplierProductId") Pageable pageable) {
        return supplierService.findProducts(supplierId, productId, active, pageable);
    }
}
