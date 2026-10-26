package com.ecommerce.supplier.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.supplier.dto.SupplierDtos.ChangeSupplierStatusRequest;
import com.ecommerce.supplier.dto.SupplierDtos.CreateSupplierRequest;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierProductRequest;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierProductResponse;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierResponse;
import com.ecommerce.supplier.dto.SupplierDtos.UpdateSupplierProductRequest;
import com.ecommerce.supplier.dto.SupplierDtos.UpdateSupplierRequest;
import com.ecommerce.supplier.mapper.SupplierMapper;
import com.ecommerce.supplier.model.PurchaseOrderStatus;
import com.ecommerce.supplier.model.Supplier;
import com.ecommerce.supplier.model.SupplierProduct;
import com.ecommerce.supplier.model.SupplierStatus;
import com.ecommerce.supplier.repository.PurchaseOrderRepository;
import com.ecommerce.supplier.repository.SupplierProductRepository;
import com.ecommerce.supplier.repository.SupplierRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

@Service
public class SupplierCatalogService {
    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<PurchaseOrderStatus> OPEN_ORDER_STATUSES = EnumSet.of(
            PurchaseOrderStatus.DRAFT,
            PurchaseOrderStatus.SUBMITTED,
            PurchaseOrderStatus.APPROVED,
            PurchaseOrderStatus.PARTIALLY_RECEIVED);

    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierMapper supplierMapper;
    private final AuditService auditService;

    public SupplierCatalogService(
            SupplierRepository supplierRepository,
            SupplierProductRepository supplierProductRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierMapper supplierMapper,
            AuditService auditService) {
        this.supplierRepository = supplierRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierMapper = supplierMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(Long supplierId) {
        return supplierMapper.toResponse(requireSupplier(supplierId));
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> findSuppliers(
            SupplierStatus status,
            String query,
            Pageable pageable) {
        validatePage(pageable);
        return PageResponse.from(
                supplierRepository.search(status, normalize(query), pageable),
                supplierMapper::toResponse);
    }

    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        String code = request.supplierCode().trim();
        if (supplierRepository.existsBySupplierCodeIgnoreCase(code)) {
            throw new BusinessRuleException(
                    "SUPPLIER_CODE_EXISTS",
                    "Supplier code is already registered");
        }
        if (supplierRepository.existsByContactEmailIgnoreCase(request.contactEmail().trim())) {
            throw new BusinessRuleException(
                    "SUPPLIER_EMAIL_EXISTS",
                    "Supplier contact email is already registered");
        }
        Supplier supplier = supplierRepository.save(supplierMapper.toEntity(request));
        audit(
                "SUPPLIER_CREATED",
                "Supplier",
                supplier.getSupplierId(),
                "supplierCode=" + supplier.getSupplierCode());
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long supplierId, UpdateSupplierRequest request) {
        Supplier supplier = requireSupplier(supplierId);
        if (supplierRepository.existsByContactEmailIgnoreCaseAndSupplierIdNot(
                request.contactEmail().trim(),
                supplierId)) {
            throw new BusinessRuleException(
                    "SUPPLIER_EMAIL_EXISTS",
                    "Supplier contact email is already registered");
        }
        supplierMapper.update(supplier, request);
        Supplier saved = supplierRepository.save(supplier);
        audit("SUPPLIER_UPDATED", "Supplier", supplierId, "supplierCode=" + saved.getSupplierCode());
        return supplierMapper.toResponse(saved);
    }

    @Transactional
    public SupplierResponse changeStatus(
            Long supplierId,
            ChangeSupplierStatusRequest request) {
        Supplier supplier = requireSupplier(supplierId);
        if (request.status() != SupplierStatus.ACTIVE
                && purchaseOrderRepository.existsBySupplierIdAndPurchaseOrderStatusIn(
                        supplierId,
                        OPEN_ORDER_STATUSES)) {
            throw new BusinessRuleException(
                    "SUPPLIER_HAS_OPEN_ORDERS",
                    "Supplier cannot be suspended while purchase orders are open");
        }
        SupplierStatus previous = supplier.getSupplierStatus();
        supplier.changeStatus(request.status());
        Supplier saved = supplierRepository.save(supplier);
        audit(
                "SUPPLIER_STATUS_CHANGED",
                "Supplier",
                supplierId,
                "from=" + previous + ",to=" + request.status());
        return supplierMapper.toResponse(saved);
    }

    @Transactional
    public SupplierProductResponse addProduct(
            Long supplierId,
            SupplierProductRequest request) {
        Supplier supplier = requireSupplier(supplierId);
        if (supplier.getSupplierStatus() != SupplierStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "SUPPLIER_NOT_ACTIVE",
                    "Products can only be added to active suppliers");
        }
        if (supplierProductRepository.existsBySupplierIdAndProductId(
                supplierId,
                request.productId())) {
            throw new BusinessRuleException(
                    "SUPPLIER_PRODUCT_EXISTS",
                    "Supplier already has a catalog entry for this product");
        }
        if (supplierProductRepository.existsBySupplierIdAndSupplierSkuIgnoreCase(
                supplierId,
                request.supplierSku())) {
            throw new BusinessRuleException(
                    "SUPPLIER_SKU_EXISTS",
                    "Supplier SKU is already in use");
        }
        SupplierProduct product = supplierProductRepository.save(
                supplierMapper.toEntity(supplierId, request));
        audit(
                "SUPPLIER_PRODUCT_ADDED",
                "SupplierProduct",
                product.getSupplierProductId(),
                "supplierId=" + supplierId + ",productId=" + request.productId());
        return supplierMapper.toResponse(product);
    }

    @Transactional
    public SupplierProductResponse updateProduct(
            Long supplierId,
            Long supplierProductId,
            UpdateSupplierProductRequest request) {
        requireSupplier(supplierId);
        SupplierProduct product = supplierProductRepository
                .findBySupplierProductIdAndSupplierId(supplierProductId, supplierId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier product " + supplierProductId + " does not belong to supplier " + supplierId));
        if (supplierProductRepository
                .existsBySupplierIdAndSupplierSkuIgnoreCaseAndSupplierProductIdNot(
                        supplierId,
                        request.supplierSku(),
                        supplierProductId)) {
            throw new BusinessRuleException(
                    "SUPPLIER_SKU_EXISTS",
                    "Supplier SKU is already in use");
        }
        supplierMapper.update(product, request);
        SupplierProduct saved = supplierProductRepository.save(product);
        audit(
                "SUPPLIER_PRODUCT_UPDATED",
                "SupplierProduct",
                supplierProductId,
                "active=" + request.active() + ",unitCost=" + request.unitCost());
        return supplierMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierProductResponse> findProducts(
            Long supplierId,
            Long productId,
            Boolean active,
            Pageable pageable) {
        if (supplierId != null) {
            requireSupplier(supplierId);
        }
        validatePage(pageable);
        return PageResponse.from(
                supplierProductRepository.search(supplierId, productId, active, pageable),
                supplierMapper::toResponse);
    }

    private Supplier requireSupplier(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Supplier page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void audit(String eventType, String entityType, Long entityId, String payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null ? "system" : authentication.getName();
        auditService.record(new WriteAuditEventRequest(
                eventType,
                entityType,
                entityId.toString(),
                actor,
                MDC.get("correlationId"),
                payload,
                null));
    }
}
