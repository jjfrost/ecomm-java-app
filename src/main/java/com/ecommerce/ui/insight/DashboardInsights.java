package com.ecommerce.ui.insight;

import java.math.BigDecimal;
import java.util.List;

public record DashboardInsights(
        List<PaymentMethodSlice> paymentMethods,
        String paymentGradient,
        long paymentCount,
        String revenueLabels,
        String revenueValues,
        BigDecimal totalRevenue,
        int revenueDayCount) {

    public record PaymentMethodSlice(
            String label,
            long count,
            int percentage,
            String color) {
    }
}
