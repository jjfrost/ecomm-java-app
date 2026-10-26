package com.ecommerce.inventory.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.inventory.domain.Warehouse;
import com.ecommerce.inventory.domain.WarehouseStatus;
import com.ecommerce.inventory.dto.WarehouseRequest;
import com.ecommerce.inventory.dto.WarehouseResponse;
import com.ecommerce.inventory.repository.WarehouseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(
            WarehouseRepository warehouseRepository,
            WarehouseMapper warehouseMapper) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseMapper = warehouseMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<WarehouseResponse> list(WarehouseStatus status, Pageable pageable) {
        Page<Warehouse> warehouses = status == null
                ? warehouseRepository.findAll(pageable)
                : warehouseRepository.findByStatus(status, pageable);
        return PageResponse.from(warehouses, warehouseMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public WarehouseResponse get(Long warehouseId) {
        return warehouseMapper.toResponse(requireWarehouse(warehouseId));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        String code = normalizeCode(request.warehouseCode());
        if (warehouseRepository.existsByWarehouseCodeIgnoreCase(code)) {
            throw new BusinessRuleException(
                    "WAREHOUSE_CODE_EXISTS",
                    "Warehouse code is already in use");
        }
        return warehouseMapper.toResponse(
                warehouseRepository.save(warehouseMapper.toEntity(request)));
    }

    @Transactional
    public WarehouseResponse update(Long warehouseId, WarehouseRequest request) {
        Warehouse warehouse = requireWarehouse(warehouseId);
        String code = normalizeCode(request.warehouseCode());
        if (warehouseRepository.existsByWarehouseCodeIgnoreCaseAndWarehouseIdNot(code, warehouseId)) {
            throw new BusinessRuleException(
                    "WAREHOUSE_CODE_EXISTS",
                    "Warehouse code is already in use");
        }
        warehouseMapper.update(warehouse, request);
        return warehouseMapper.toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional
    public void delete(Long warehouseId) {
        Warehouse warehouse = requireWarehouse(warehouseId);
        if (warehouse.getStatus() != WarehouseStatus.INACTIVE) {
            throw new BusinessRuleException(
                    "WAREHOUSE_NOT_INACTIVE",
                    "Only inactive warehouses can be deleted");
        }
        warehouseRepository.delete(warehouse);
    }

    private Warehouse requireWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", warehouseId));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}
