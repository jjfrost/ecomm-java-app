package com.ecommerce.ui.insight;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.model.PaymentMethod;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardInsightServiceTest {
    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Test
    void buildsPaymentMixAndChronologicalRevenue() {
        when(orderService.list(isNull(), isNull(), any())).thenReturn(page(List.of(
                order(1L, "2025-01-02T12:00:00Z", "100.00", OrderStatus.DELIVERED),
                order(2L, "2025-01-01T12:00:00Z", "50.00", OrderStatus.DELIVERED),
                order(3L, "2025-01-02T15:00:00Z", "25.00", OrderStatus.CANCELLED))));
        when(paymentService.findPayments(isNull(), isNull(), isNull(), any())).thenReturn(page(List.of(
                payment(1L, PaymentMethod.CARD),
                payment(2L, PaymentMethod.CARD),
                payment(3L, PaymentMethod.DIGITAL_WALLET),
                payment(4L, PaymentMethod.BANK_TRANSFER))));

        DashboardInsights insights =
                new DashboardInsightService(orderService, paymentService).load();

        assertThat(insights.paymentCount()).isEqualTo(4);
        assertThat(insights.paymentMethods())
                .extracting(DashboardInsights.PaymentMethodSlice::percentage)
                .containsExactly(50, 25, 25);
        assertThat(insights.paymentGradient()).contains("#4f46e5", "#0ea5e9", "#14b8a6");
        assertThat(insights.revenueLabels()).isEqualTo("Jan 1|Jan 2");
        assertThat(insights.revenueValues()).isEqualTo("50.00,100.00");
        assertThat(insights.totalRevenue()).isEqualByComparingTo("150.00");
        assertThat(insights.revenueDayCount()).isEqualTo(2);
    }

    @Test
    void handlesEmptyCommerceData() {
        when(orderService.list(isNull(), isNull(), any())).thenReturn(page(List.of()));
        when(paymentService.findPayments(isNull(), isNull(), isNull(), any())).thenReturn(page(List.of()));

        DashboardInsights insights =
                new DashboardInsightService(orderService, paymentService).load();

        assertThat(insights.paymentMethods()).isEmpty();
        assertThat(insights.paymentGradient()).isEqualTo("#e5e7eb 0deg 360deg");
        assertThat(insights.paymentCount()).isZero();
        assertThat(insights.totalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(insights.revenueLabels()).isEmpty();
    }

    private OrderResponse order(
            Long id,
            String timestamp,
            String total,
            OrderStatus status) {
        Instant time = Instant.parse(timestamp);
        return new OrderResponse(
                id,
                5001L,
                time,
                status,
                new BigDecimal(total),
                List.of(),
                time,
                time);
    }

    private PaymentResponse payment(Long id, PaymentMethod method) {
        return new PaymentResponse(
                id,
                25001L,
                5001L,
                method,
                new BigDecimal("25.00"),
                Instant.EPOCH,
                "CONF",
                Instant.EPOCH,
                "TXN-" + id,
                PaymentStatus.SETTLED,
                null);
    }

    private <T> PageResponse<T> page(List<T> content) {
        return new PageResponse<>(
                content,
                0,
                100,
                content.size(),
                content.isEmpty() ? 0 : 1,
                true,
                true);
    }
}
