package com.ecommerce.returns.mapper;

import com.ecommerce.returns.dto.ReturnDtos.ReturnItemResponse;
import com.ecommerce.returns.dto.ReturnDtos.ReturnRequestResponse;
import com.ecommerce.returns.model.ReturnItem;
import com.ecommerce.returns.model.ReturnRequest;
import org.springframework.stereotype.Component;

@Component
public class ReturnMapper {
    public ReturnRequestResponse toResponse(ReturnRequest request) {
        return new ReturnRequestResponse(
                request.getReturnRequestId(),
                request.getOrderId(),
                request.getCustomerId(),
                request.getReturnReason(),
                request.getReturnStatus(),
                request.getAuthorizationCode(),
                request.getCustomerNotes(),
                request.getRequestedAt(),
                request.getAuthorizedAt(),
                request.getReceivedAt(),
                request.getCompletedAt());
    }

    public ReturnItemResponse toResponse(ReturnItem item) {
        return new ReturnItemResponse(
                item.getReturnItemId(),
                item.getReturnRequestId(),
                item.getOrderItemId(),
                item.getProductId(),
                item.getQuantity(),
                item.getItemCondition(),
                item.getDisposition(),
                item.getRefundAmount());
    }
}
