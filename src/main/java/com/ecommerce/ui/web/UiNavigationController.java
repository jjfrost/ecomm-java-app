package com.ecommerce.ui.web;

import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.customer.service.CustomerService;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.ui.oracle.OracleConnectionProbe;
import com.ecommerce.ui.insight.DashboardInsightService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiNavigationController {
    private final CustomerService customerService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final OracleConnectionProbe oracleConnectionProbe;
    private final DashboardInsightService dashboardInsightService;

    public UiNavigationController(
            CustomerService customerService,
            ProductService productService,
            InventoryService inventoryService,
            OrderService orderService,
            PaymentService paymentService,
            OracleConnectionProbe oracleConnectionProbe,
            DashboardInsightService dashboardInsightService) {
        this.customerService = customerService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.oracleConnectionProbe = oracleConnectionProbe;
        this.dashboardInsightService = dashboardInsightService;
    }

    @GetMapping("/")
    String root() {
        return "redirect:/ui/dashboard";
    }

    @GetMapping("/ui/dashboard")
    String dashboard(Model model) {
        PageRequest recent = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt"));
        PageResponse<?> customers = customerService.search(null, null, recent);
        PageResponse<?> products = productService.search(null, null, null, null, null, recent);
        PageResponse<?> inventory = inventoryService.list(
                null,
                null,
                false,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "lastUpdated")));
        PageResponse<OrderResponse> orders = orderService.list(
                null,
                null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "orderDate")));
        PageResponse<PaymentResponse> payments = paymentService.findPayments(
                null,
                null,
                null,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt")));

        model.addAttribute("activeNav", "dashboard");
        model.addAttribute("customersCount", customers.totalElements());
        model.addAttribute("productsCount", products.totalElements());
        model.addAttribute("inventoryCount", inventory.totalElements());
        model.addAttribute("ordersCount", orders.totalElements());
        model.addAttribute("paymentsCount", payments.totalElements());
        model.addAttribute("recentOrders", orders.content());
        model.addAttribute("recentPayments", payments.content());
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("oracle", oracleConnectionProbe.currentConnection());
        model.addAttribute("insights", dashboardInsightService.load());
        return "dashboard";
    }
}
