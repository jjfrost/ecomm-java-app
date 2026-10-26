package com.ecommerce.ui.form;

import com.ecommerce.catalog.api.dto.ProductDetailResponse;
import com.ecommerce.customer.api.dto.CustomerDetailResponse;
import com.ecommerce.customer.api.dto.HomeAddressResponse;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.payment.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UiFormTest {
    @Test
    void mapsCustomerDetailToUpdateRequest() {
        CustomerDetailResponse customer = new CustomerDetailResponse(
                5001L,
                "Ada Lovelace",
                "ada@example.test",
                new HomeAddressResponse("1 Main Street", "London", "10001"),
                true,
                Instant.EPOCH,
                Instant.EPOCH,
                List.of(),
                List.of());

        CustomerEditForm form = CustomerEditForm.from(customer);
        form.setCity("Oxford");

        assertThat(form.toRequest().name()).isEqualTo("Ada Lovelace");
        assertThat(form.toRequest().homeAddress().city()).isEqualTo("Oxford");
    }

    @Test
    void mapsProductDetailToUpdateRequest() {
        ProductDetailResponse product = new ProductDetailResponse(
                7001L,
                "SKU-001",
                "Reference Product",
                "Description",
                new BigDecimal("99.95"),
                true,
                6001L,
                "Reference Category",
                Instant.EPOCH,
                Instant.EPOCH,
                List.of(),
                List.of());

        ProductEditForm form = ProductEditForm.from(product);
        form.setPrice(new BigDecimal("109.95"));

        assertThat(form.getName()).isEqualTo("Reference Product");
        assertThat(form.toRequest().price()).isEqualByComparingTo("109.95");
        assertThat(form.toRequest().categoryId()).isEqualTo(6001L);
    }

    @Test
    void mapsInventoryAdjustmentToDomainRequest() {
        InventoryAdjustmentForm form = new InventoryAdjustmentForm();
        form.setQuantityDelta(new BigDecimal("-2.50"));
        form.setReason("Cycle count correction");
        form.setPerformedBy("operator");

        assertThat(form.toRequest().quantityDelta()).isEqualByComparingTo("-2.50");
        assertThat(form.toRequest().reason()).isEqualTo("Cycle count correction");
        assertThat(form.toRequest().performedBy()).isEqualTo("operator");
    }

    @Test
    void mapsOrderTransitionToDomainRequest() {
        OrderStatusForm form = new OrderStatusForm();
        form.setStatus(OrderStatus.CONFIRMED);
        form.setChangedBy("operator");
        form.setNotes("Payment authorized");

        assertThat(form.toRequest().status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(form.toRequest().changedBy()).isEqualTo("operator");
        assertThat(form.toRequest().notes()).isEqualTo("Payment authorized");
    }

    @Test
    void mapsPaymentTimestampAsUtc() {
        PaymentStatusForm form = new PaymentStatusForm();
        form.setStatus(PaymentStatus.SETTLED);
        form.setConfirmation("CONF-100");
        form.setFailureReason(null);
        form.setPaidAt(LocalDateTime.of(2025, 1, 15, 12, 30));

        assertThat(form.toRequest().status()).isEqualTo(PaymentStatus.SETTLED);
        assertThat(form.toRequest().confirmation()).isEqualTo("CONF-100");
        assertThat(form.toRequest().paidAt()).isEqualTo("2025-01-15T12:30:00Z");
    }
}
