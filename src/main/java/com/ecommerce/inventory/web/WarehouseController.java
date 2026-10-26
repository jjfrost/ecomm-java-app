package com.ecommerce.inventory.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.inventory.domain.WarehouseStatus;
import com.ecommerce.inventory.dto.WarehouseRequest;
import com.ecommerce.inventory.dto.WarehouseResponse;
import com.ecommerce.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    public PageResponse<WarehouseResponse> list(
            @RequestParam(required = false) WarehouseStatus status,
            Pageable pageable) {
        return warehouseService.list(status, pageable);
    }

    @GetMapping("/{warehouseId}")
    public WarehouseResponse get(@PathVariable @Positive Long warehouseId) {
        return warehouseService.get(warehouseId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ResponseEntity<WarehouseResponse> create(
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.create(request));
    }

    @PutMapping("/{warehouseId}")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public WarehouseResponse update(
            @PathVariable @Positive Long warehouseId,
            @Valid @RequestBody WarehouseRequest request) {
        return warehouseService.update(warehouseId, request);
    }

    @DeleteMapping("/{warehouseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long warehouseId) {
        warehouseService.delete(warehouseId);
        return ResponseEntity.noContent().build();
    }
}
