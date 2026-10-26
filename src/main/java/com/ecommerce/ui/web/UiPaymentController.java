package com.ecommerce.ui.web;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.oracle.OracleRoutineException;
import com.ecommerce.payment.dto.PaymentDtos.PaymentResponse;
import com.ecommerce.payment.model.PaymentStatus;
import com.ecommerce.payment.service.PaymentService;
import com.ecommerce.ui.form.PaymentStatusForm;
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

import java.util.List;

@Controller
@RequestMapping("/ui/payments")
public class UiPaymentController {
    private final PaymentService paymentService;

    public UiPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) PaymentStatus status,
            Model model) {
        model.addAttribute(
                "page",
                paymentService.findPayments(
                        null,
                        null,
                        status,
                        PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("activeNav", "payments");
        return "payments/list";
    }

    @GetMapping("/{paymentId}/edit")
    String edit(@PathVariable Long paymentId, Model model) {
        PaymentResponse payment = paymentService.getPayment(paymentId);
        if (!model.containsAttribute("form")) {
            PaymentStatusForm form = new PaymentStatusForm();
            form.setConfirmation(payment.confirmation());
            model.addAttribute("form", form);
        }
        populateEditModel(model, payment);
        return "payments/edit";
    }

    @PostMapping("/{paymentId}/status")
    String update(
            @PathVariable Long paymentId,
            @Valid @ModelAttribute("form") PaymentStatusForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateEditModel(model, paymentService.getPayment(paymentId));
            return "payments/edit";
        }
        try {
            paymentService.updateStatus(paymentId, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Payment status updated.");
            return "redirect:/ui/payments";
        } catch (BusinessRuleException | ResourceNotFoundException | OracleRoutineException exception) {
            model.addAttribute("error", exception.getMessage());
            populateEditModel(model, paymentService.getPayment(paymentId));
            return "payments/edit";
        }
    }

    private void populateEditModel(Model model, PaymentResponse payment) {
        model.addAttribute("payment", payment);
        model.addAttribute(
                "statuses",
                permittedTransitions(payment.paymentStatus()));
        model.addAttribute("activeNav", "payments");
    }

    private List<PaymentStatus> permittedTransitions(PaymentStatus current) {
        return switch (current) {
            case PENDING -> List.of(
                    PaymentStatus.AUTHORIZED,
                    PaymentStatus.FAILED,
                    PaymentStatus.VOIDED);
            case AUTHORIZED -> List.of(
                    PaymentStatus.CAPTURED,
                    PaymentStatus.FAILED,
                    PaymentStatus.VOIDED);
            case CAPTURED -> List.of(
                    PaymentStatus.SETTLED,
                    PaymentStatus.PARTIALLY_REFUNDED,
                    PaymentStatus.REFUNDED);
            case SETTLED -> List.of(
                    PaymentStatus.PARTIALLY_REFUNDED,
                    PaymentStatus.REFUNDED);
            case FAILED -> List.of(PaymentStatus.PENDING, PaymentStatus.AUTHORIZED);
            case PARTIALLY_REFUNDED -> List.of(PaymentStatus.REFUNDED);
            case VOIDED, REFUNDED -> List.of();
        };
    }
}
