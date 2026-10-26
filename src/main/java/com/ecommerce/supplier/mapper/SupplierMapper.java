package com.ecommerce.supplier.mapper;

import com.ecommerce.supplier.dto.SupplierDtos.CreateSupplierRequest;
import com.ecommerce.supplier.dto.SupplierDtos.GoodsReceiptResponse;
import com.ecommerce.supplier.dto.SupplierDtos.PurchaseOrderItemResponse;
import com.ecommerce.supplier.dto.SupplierDtos.PurchaseOrderResponse;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierProductRequest;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierProductResponse;
import com.ecommerce.supplier.dto.SupplierDtos.SupplierResponse;
import com.ecommerce.supplier.dto.SupplierDtos.UpdateSupplierProductRequest;
import com.ecommerce.supplier.dto.SupplierDtos.UpdateSupplierRequest;
import com.ecommerce.supplier.model.GoodsReceipt;
import com.ecommerce.supplier.model.PurchaseOrder;
import com.ecommerce.supplier.model.PurchaseOrderItem;
import com.ecommerce.supplier.model.Supplier;
import com.ecommerce.supplier.model.SupplierProduct;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {
    public Supplier toEntity(CreateSupplierRequest request) {
        return new Supplier(
                request.supplierCode().trim().toUpperCase(),
                request.supplierName().trim(),
                normalize(request.contactName()),
                request.contactEmail().trim().toLowerCase(),
                normalize(request.contactPhone()),
                request.leadTimeDays(),
                request.paymentTerms().trim());
    }

    public void update(Supplier supplier, UpdateSupplierRequest request) {
        supplier.update(
                request.supplierName().trim(),
                normalize(request.contactName()),
                request.contactEmail().trim().toLowerCase(),
                normalize(request.contactPhone()),
                request.leadTimeDays(),
                request.paymentTerms().trim());
    }

    public SupplierProduct toEntity(
            Long supplierId,
            SupplierProductRequest request) {
        return new SupplierProduct(
                supplierId,
                request.productId(),
                request.supplierSku().trim(),
                request.unitCost(),
                request.minimumOrderQuantity(),
                request.leadTimeDays());
    }

    public void update(SupplierProduct product, UpdateSupplierProductRequest request) {
        product.update(
                request.supplierSku().trim(),
                request.unitCost(),
                request.minimumOrderQuantity(),
                request.leadTimeDays(),
                request.active());
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getSupplierId(),
                supplier.getSupplierCode(),
                supplier.getSupplierName(),
                supplier.getContactName(),
                supplier.getContactEmail(),
                supplier.getContactPhone(),
                supplier.getSupplierStatus(),
                supplier.getLeadTimeDays(),
                supplier.getPaymentTerms(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt());
    }

    public SupplierProductResponse toResponse(SupplierProduct product) {
        return new SupplierProductResponse(
                product.getSupplierProductId(),
                product.getSupplierId(),
                product.getProductId(),
                product.getSupplierSku(),
                product.getUnitCost(),
                product.getMinimumOrderQuantity(),
                product.getLeadTimeDays(),
                product.getActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    public PurchaseOrderResponse toResponse(PurchaseOrder purchaseOrder) {
        return new PurchaseOrderResponse(
                purchaseOrder.getPurchaseOrderId(),
                purchaseOrder.getPoNumber(),
                purchaseOrder.getSupplierId(),
                purchaseOrder.getWarehouseId(),
                purchaseOrder.getPurchaseOrderStatus(),
                purchaseOrder.getOrderDate(),
                purchaseOrder.getExpectedDate(),
                purchaseOrder.getSubtotal(),
                purchaseOrder.getTaxAmount(),
                purchaseOrder.getShippingAmount(),
                purchaseOrder.getTotalAmount(),
                purchaseOrder.getCreatedBy(),
                purchaseOrder.getApprovedBy(),
                purchaseOrder.getApprovedAt(),
                purchaseOrder.getCreatedAt(),
                purchaseOrder.getUpdatedAt());
    }

    public PurchaseOrderItemResponse toResponse(PurchaseOrderItem item) {
        return new PurchaseOrderItemResponse(
                item.getPurchaseOrderItemId(),
                item.getPurchaseOrderId(),
                item.getSupplierProductId(),
                item.getProductId(),
                item.getOrderedQuantity(),
                item.getReceivedQuantity(),
                item.getUnitCost(),
                item.getLineTotal());
    }

    public GoodsReceiptResponse toResponse(GoodsReceipt receipt) {
        return new GoodsReceiptResponse(
                receipt.getGoodsReceiptId(),
                receipt.getPurchaseOrderId(),
                receipt.getPurchaseOrderItemId(),
                receipt.getProductId(),
                receipt.getReceivedQuantity(),
                receipt.getRejectedQuantity(),
                receipt.getReceiptReference(),
                receipt.getReceivedBy(),
                receipt.getReceivedAt(),
                receipt.getNotes());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
