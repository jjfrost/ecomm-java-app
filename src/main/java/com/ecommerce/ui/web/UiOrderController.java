package com.ecommerce.ui.web;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.oracle.OracleRoutineException;
import com.ecommerce.order.domain.OrderStatus;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.ui.form.OrderStatusForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/ui/orders")
public class UiOrderController {
    private final OrderService orderService;

    public UiOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) OrderStatus status,
            Model model) {
        model.addAttribute(
                "page",
                orderService.list(
                        null,
                        status,
                        PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "orderDate"))));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("activeNav", "orders");
        return "orders/list";
    }

    @GetMapping("/{orderId}/edit")
    String edit(
            @PathVariable Long orderId,
            Model model) {
        OrderResponse order = orderService.get(orderId);
        if (!model.containsAttribute("form")) {
            OrderStatusForm form = new OrderStatusForm();
            form.setChangedBy("demo-operator");
            model.addAttribute("form", form);
        }
        populateEditModel(model, order);
        return "orders/edit";
    }

    @PostMapping("/{orderId}/status")
    String transition(
            @PathVariable Long orderId,
            @Valid @ModelAttribute("form") OrderStatusForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        OrderResponse order = orderService.get(orderId);
        if (bindingResult.hasErrors()) {
            populateEditModel(model, order);
            return "orders/edit";
        }
        try {
            orderService.transition(orderId, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Order status updated.");
            return "redirect:/ui/orders";
        } catch (BusinessRuleException | ResourceNotFoundException | OracleRoutineException exception) {
            model.addAttribute("error", exception.getMessage());
            populateEditModel(model, orderService.get(orderId));
            return "orders/edit";
        }
    }

    private void populateEditModel(Model model, OrderResponse order) {
        List<OrderStatus> transitions = Arrays.stream(OrderStatus.values())
                .filter(order.status()::canTransitionTo)
                .filter(status -> status != OrderStatus.CANCELLED)
                .toList();
        model.addAttribute("order", order);
        model.addAttribute("transitions", transitions);
        model.addAttribute("activeNav", "orders");
    }
}
