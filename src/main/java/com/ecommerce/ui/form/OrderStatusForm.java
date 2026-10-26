package com.ecommerce.ui.form;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderStatusTransitionRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class OrderStatusForm {
    @NotNull
    private OrderStatus status;

    @NotBlank
    @Size(max = 100)
    private String changedBy;

    @Size(max = 500)
    private String notes;

    public OrderStatusTransitionRequest toRequest() {
        return new OrderStatusTransitionRequest(status, changedBy, notes);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
