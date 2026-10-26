package com.ecommerce.ui.form;

import com.ecommerce.inventory.dto.StockAdjustmentRequest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class InventoryAdjustmentForm {
    @NotNull
    @DecimalMin("-1000000.00")
    @DecimalMax("1000000.00")
    private BigDecimal quantityDelta;

    @NotBlank
    @Size(max = 500)
    private String reason;

    @NotBlank
    @Size(max = 100)
    private String performedBy;

    public StockAdjustmentRequest toRequest() {
        return new StockAdjustmentRequest(quantityDelta, reason, performedBy);
    }

    public BigDecimal getQuantityDelta() {
        return quantityDelta;
    }

    public void setQuantityDelta(BigDecimal quantityDelta) {
        this.quantityDelta = quantityDelta;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }
}
