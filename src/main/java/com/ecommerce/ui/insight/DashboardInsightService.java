package com.ecommerce.ui.insight;

import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.model.PaymentMethod;
import com.ecommerce.payment.service.PaymentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class DashboardInsightService {
    private static final List<String> PAYMENT_COLORS = List.of(
            "#4f46e5",
            "#0ea5e9",
            "#14b8a6",
            "#f59e0b",
            "#ec4899");
    private static final DateTimeFormatter DAY_LABEL =
            DateTimeFormatter.ofPattern("MMM d", Locale.US);

    private final OrderService orderService;
    private final PaymentService paymentService;

    public DashboardInsightService(
            OrderService orderService,
            PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    public DashboardInsights load() {
        List<OrderResponse> orders = orderService.list(
                        null,
                        null,
                        PageRequest.of(0, 100, Sort.by("orderDate")))
                .content();
        List<PaymentResponse> payments = paymentService.findPayments(
                        null,
                        null,
                        null,
                        PageRequest.of(0, 100, Sort.by("createdAt")))
                .content();

        List<DashboardInsights.PaymentMethodSlice> paymentSlices =
                paymentMethodSlices(payments);
        Map<LocalDate, BigDecimal> revenueByDay = revenueByDay(orders);

        String labels = revenueByDay.keySet()
                .stream()
                .map(DAY_LABEL::format)
                .collect(Collectors.joining("|"));
        String values = revenueByDay.values()
                .stream()
                .map(value -> value.setScale(2, RoundingMode.HALF_UP).toPlainString())
                .collect(Collectors.joining(","));
        BigDecimal totalRevenue = revenueByDay.values()
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardInsights(
                paymentSlices,
                gradient(paymentSlices),
                payments.size(),
                labels,
                values,
                totalRevenue,
                revenueByDay.size());
    }

    private List<DashboardInsights.PaymentMethodSlice> paymentMethodSlices(
            List<PaymentResponse> payments) {
        Map<PaymentMethod, Long> counts = new EnumMap<>(PaymentMethod.class);
        payments.forEach(payment -> counts.merge(payment.paymentMethod(), 1L, Long::sum));
        long total = payments.size();
        List<DashboardInsights.PaymentMethodSlice> slices = new ArrayList<>();
        int colorIndex = 0;
        for (PaymentMethod method : PaymentMethod.values()) {
            long count = counts.getOrDefault(method, 0L);
            if (count == 0) {
                continue;
            }
            int percentage = total == 0 ? 0 : (int) Math.round((count * 100.0) / total);
            slices.add(new DashboardInsights.PaymentMethodSlice(
                    method.name().replace('_', ' '),
                    count,
                    percentage,
                    PAYMENT_COLORS.get(colorIndex % PAYMENT_COLORS.size())));
            colorIndex++;
        }
        return List.copyOf(slices);
    }

    private Map<LocalDate, BigDecimal> revenueByDay(List<OrderResponse> orders) {
        Map<LocalDate, BigDecimal> revenue = new TreeMap<>();
        orders.stream()
                .filter(order -> order.status() != OrderStatus.CANCELLED)
                .forEach(order -> revenue.merge(
                        order.orderDate().atZone(ZoneOffset.UTC).toLocalDate(),
                        order.totalAmount(),
                        BigDecimal::add));
        return revenue;
    }

    private String gradient(List<DashboardInsights.PaymentMethodSlice> slices) {
        if (slices.isEmpty()) {
            return "#e5e7eb 0deg 360deg";
        }
        double start = 0;
        List<String> stops = new ArrayList<>();
        for (DashboardInsights.PaymentMethodSlice slice : slices) {
            double end = start + (slice.percentage() * 3.6);
            stops.add("%s %.2fdeg %.2fdeg".formatted(slice.color(), start, end));
            start = end;
        }
        if (start < 360) {
            DashboardInsights.PaymentMethodSlice last = slices.get(slices.size() - 1);
            stops.add("%s %.2fdeg 360deg".formatted(last.color(), start));
        }
        return String.join(", ", stops);
    }
}
