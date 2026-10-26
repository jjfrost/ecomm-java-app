package com.ecommerce.supplier.service;

import com.ecommerce.audit.dto.AuditDtos.WriteAuditEventRequest;
import com.ecommerce.audit.service.AuditService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.supplier.dto.SupplierDtos.ApprovePurchaseOrderRequest;
import com.ecommerce.supplier.dto.SupplierDtos.CreatePurchaseOrderRequest;
import com.ecommerce.supplier.dto.SupplierDtos.GoodsReceiptResponse;
import com.ecommerce.supplier.dto.SupplierDtos.PurchaseOrderDetailResponse;
import com.ecommerce.supplier.dto.SupplierDtos.PurchaseOrderResponse;
import com.ecommerce.supplier.dto.SupplierDtos.ReceiveGoodsRequest;
import com.ecommerce.supplier.gateway.ProcurementPackageGateway;
import com.ecommerce.supplier.mapper.SupplierMapper;
import com.ecommerce.supplier.model.GoodsReceipt;
import com.ecommerce.supplier.model.PurchaseOrder;
import com.ecommerce.supplier.model.PurchaseOrderItem;
import com.ecommerce.supplier.model.PurchaseOrderStatus;
import com.ecommerce.supplier.model.Supplier;
import com.ecommerce.supplier.model.SupplierProduct;
import com.ecommerce.supplier.model.SupplierStatus;
import com.ecommerce.supplier.repository.GoodsReceiptRepository;
import com.ecommerce.supplier.repository.PurchaseOrderItemRepository;
import com.ecommerce.supplier.repository.PurchaseOrderRepository;
import com.ecommerce.supplier.repository.SupplierProductRepository;
import com.ecommerce.supplier.repository.SupplierRepository;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProcurementService {
    private static final int MAX_PAGE_SIZE = 200;

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final ProcurementPackageGateway procurementGateway;
    private final SupplierMapper supplierMapper;
    private final AuditService auditService;

    public ProcurementService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderItemRepository purchaseOrderItemRepository,
            GoodsReceiptRepository goodsReceiptRepository,
            SupplierRepository supplierRepository,
            SupplierProductRepository supplierProductRepository,
            ProcurementPackageGateway procurementGateway,
            SupplierMapper supplierMapper,
            AuditService auditService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderItemRepository = purchaseOrderItemRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.supplierRepository = supplierRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.procurementGateway = procurementGateway;
        this.supplierMapper = supplierMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = requirePurchaseOrder(purchaseOrderId);
        var items = purchaseOrderItemRepository
                .findByPurchaseOrderIdOrderByPurchaseOrderItemId(purchaseOrderId)
                .stream()
                .map(supplierMapper::toResponse)
                .toList();
        var receipts = goodsReceiptRepository
                .findByPurchaseOrderIdOrderByReceivedAtAsc(purchaseOrderId)
                .stream()
                .map(supplierMapper::toResponse)
                .toList();
        return new PurchaseOrderDetailResponse(
                supplierMapper.toResponse(purchaseOrder),
                items,
                receipts);
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> findPurchaseOrders(
            Long supplierId,
            Long warehouseId,
            PurchaseOrderStatus status,
            Pageable pageable) {
        validatePage(pageable);
        return PageResponse.from(
                purchaseOrderRepository.search(supplierId, warehouseId, status, pageable),
                supplierMapper::toResponse);
    }

    @Transactional
    public PurchaseOrderDetailResponse createPurchaseOrder(
            CreatePurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.supplierId()));
        if (supplier.getSupplierStatus() != SupplierStatus.ACTIVE) {
            throw new BusinessRuleException(
                    "SUPPLIER_NOT_ACTIVE",
                    "Purchase orders require an active supplier");
        }
        if (request.expectedDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException(
                    "EXPECTED_DATE_IN_PAST",
                    "Expected delivery date cannot be in the past");
        }
        validateOrderItems(request);
        Long purchaseOrderId = procurementGateway.createPurchaseOrder(
                request,
                currentActor());
        request.items().forEach(item -> procurementGateway.addPurchaseOrderItem(
                purchaseOrderId,
                item.supplierProductId(),
                item.quantity()));
        audit(
                "PURCHASE_ORDER_CREATED",
                purchaseOrderId,
                "supplierId=" + request.supplierId() + ",itemCount=" + request.items().size());
        return getPurchaseOrder(purchaseOrderId);
    }

    @Transactional
    public PurchaseOrderResponse submit(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = lockPurchaseOrder(purchaseOrderId);
        requireState(purchaseOrder, Set.of(PurchaseOrderStatus.DRAFT), "submit");
        if (purchaseOrderItemRepository.countByPurchaseOrderId(purchaseOrderId) == 0) {
            throw new BusinessRuleException(
                    "PURCHASE_ORDER_EMPTY",
                    "Purchase order must contain at least one item");
        }
        procurementGateway.submit(purchaseOrderId);
        audit("PURCHASE_ORDER_SUBMITTED", purchaseOrderId, "poNumber=" + purchaseOrder.getPoNumber());
        return supplierMapper.toResponse(requirePurchaseOrder(purchaseOrderId));
    }

    @Transactional
    public PurchaseOrderResponse approve(
            Long purchaseOrderId,
            ApprovePurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = lockPurchaseOrder(purchaseOrderId);
        requireState(purchaseOrder, Set.of(PurchaseOrderStatus.SUBMITTED), "approve");
        procurementGateway.approve(purchaseOrderId, request.approvedBy());
        audit(
                "PURCHASE_ORDER_APPROVED",
                purchaseOrderId,
                "approvedBy=" + request.approvedBy());
        return supplierMapper.toResponse(requirePurchaseOrder(purchaseOrderId));
    }

    @Transactional
    public GoodsReceiptResponse receiveGoods(
            Long purchaseOrderId,
            ReceiveGoodsRequest request) {
        PurchaseOrder purchaseOrder = lockPurchaseOrder(purchaseOrderId);
        requireState(
                purchaseOrder,
                Set.of(
                        PurchaseOrderStatus.APPROVED,
                        PurchaseOrderStatus.PARTIALLY_RECEIVED),
                "receive goods against");
        if (goodsReceiptRepository.existsByReceiptReference(request.receiptReference())) {
            throw new BusinessRuleException(
                    "RECEIPT_REFERENCE_EXISTS",
                    "Receipt reference has already been processed");
        }
        if (request.receivedAt().isAfter(Instant.now())) {
            throw new BusinessRuleException(
                    "RECEIPT_TIME_IN_FUTURE",
                    "Goods receipt time cannot be in the future");
        }
        PurchaseOrderItem item = purchaseOrderItemRepository
                .findByPurchaseOrderItemIdAndPurchaseOrderId(
                        request.purchaseOrderItemId(),
                        purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase order item " + request.purchaseOrderItemId()
                                + " does not belong to purchase order " + purchaseOrderId));
        int remaining = item.getOrderedQuantity() - item.getReceivedQuantity();
        if (request.receivedQuantity() > remaining) {
            throw new BusinessRuleException(
                    "RECEIPT_EXCEEDS_ORDERED_QUANTITY",
                    "Accepted quantity cannot exceed the remaining ordered quantity");
        }
        Long receiptId = procurementGateway.receiveGoods(purchaseOrderId, request);
        GoodsReceipt receipt = goodsReceiptRepository.findById(receiptId)
                .orElseThrow(() -> new ResourceNotFoundException("Goods receipt", receiptId));
        audit(
                "GOODS_RECEIVED",
                purchaseOrderId,
                "receiptId=" + receiptId + ",itemId=" + request.purchaseOrderItemId()
                        + ",quantity=" + request.receivedQuantity());
        return supplierMapper.toResponse(receipt);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long purchaseOrderId, String reason) {
        PurchaseOrder purchaseOrder = lockPurchaseOrder(purchaseOrderId);
        requireState(
                purchaseOrder,
                Set.of(
                        PurchaseOrderStatus.DRAFT,
                        PurchaseOrderStatus.SUBMITTED,
                        PurchaseOrderStatus.APPROVED),
                "cancel");
        if (goodsReceiptRepository.existsByPurchaseOrderId(purchaseOrderId)) {
            throw new BusinessRuleException(
                    "PURCHASE_ORDER_HAS_RECEIPTS",
                    "Purchase order with goods receipts cannot be cancelled");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessRuleException(
                    "PURCHASE_ORDER_CANCELLATION_REASON_REQUIRED",
                    "Purchase order cancellation requires a reason");
        }
        procurementGateway.cancel(purchaseOrderId, reason.trim());
        audit("PURCHASE_ORDER_CANCELLED", purchaseOrderId, "reason=" + reason.trim());
        return supplierMapper.toResponse(requirePurchaseOrder(purchaseOrderId));
    }

    @Transactional
    public PurchaseOrderResponse close(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = lockPurchaseOrder(purchaseOrderId);
        requireState(purchaseOrder, Set.of(PurchaseOrderStatus.RECEIVED), "close");
        procurementGateway.close(purchaseOrderId);
        audit("PURCHASE_ORDER_CLOSED", purchaseOrderId, "poNumber=" + purchaseOrder.getPoNumber());
        return supplierMapper.toResponse(requirePurchaseOrder(purchaseOrderId));
    }

    private void validateOrderItems(CreatePurchaseOrderRequest request) {
        Set<Long> uniqueCatalogItems = new HashSet<>();
        for (var item : request.items()) {
            if (!uniqueCatalogItems.add(item.supplierProductId())) {
                throw new BusinessRuleException(
                        "DUPLICATE_PURCHASE_ORDER_ITEM",
                        "Each supplier catalog item can appear only once");
            }
            SupplierProduct product = supplierProductRepository.findById(item.supplierProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Supplier product",
                            item.supplierProductId()));
            if (!product.getSupplierId().equals(request.supplierId())) {
                throw new BusinessRuleException(
                        "SUPPLIER_PRODUCT_MISMATCH",
                        "All purchase order items must belong to the selected supplier");
            }
            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new BusinessRuleException(
                        "SUPPLIER_PRODUCT_INACTIVE",
                        "Inactive supplier products cannot be ordered");
            }
            if (item.quantity() < product.getMinimumOrderQuantity()) {
                throw new BusinessRuleException(
                        "BELOW_MINIMUM_ORDER_QUANTITY",
                        "Quantity for supplier product "
                                + item.supplierProductId()
                                + " must be at least "
                                + product.getMinimumOrderQuantity());
            }
        }
    }

    private PurchaseOrder lockPurchaseOrder(Long purchaseOrderId) {
        return purchaseOrderRepository.findByIdForUpdate(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order", purchaseOrderId));
    }

    private PurchaseOrder requirePurchaseOrder(Long purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order", purchaseOrderId));
    }

    private void requireState(
            PurchaseOrder purchaseOrder,
            Set<PurchaseOrderStatus> expected,
            String operation) {
        if (!expected.contains(purchaseOrder.getPurchaseOrderStatus())) {
            throw new BusinessRuleException(
                    "INVALID_PURCHASE_ORDER_STATE",
                    "Cannot " + operation + " purchase order in "
                            + purchaseOrder.getPurchaseOrderStatus()
                            + " state");
        }
    }

    private void validatePage(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Purchase order page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? "system" : authentication.getName();
    }

    private void audit(String eventType, Long purchaseOrderId, String payload) {
        auditService.record(new WriteAuditEventRequest(
                eventType,
                "PurchaseOrder",
                purchaseOrderId.toString(),
                currentActor(),
                MDC.get("correlationId"),
                payload,
                null));
    }
}
