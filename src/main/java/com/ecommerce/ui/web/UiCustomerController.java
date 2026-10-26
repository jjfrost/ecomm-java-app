package com.ecommerce.ui.web;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.oracle.OracleRoutineException;
import com.ecommerce.customer.api.dto.CustomerDetailResponse;
import com.ecommerce.customer.service.CustomerService;
import com.ecommerce.ui.form.CustomerEditForm;
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

@Controller
@RequestMapping("/ui/customers")
public class UiCustomerController {
    private final CustomerService customerService;

    public UiCustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search,
            Model model) {
        model.addAttribute(
                "page",
                customerService.search(
                        search,
                        null,
                        PageRequest.of(page, 10, Sort.by("name"))));
        model.addAttribute("search", search);
        model.addAttribute("activeNav", "customers");
        return "customers/list";
    }

    @GetMapping("/{customerId}/edit")
    String edit(@PathVariable Long customerId, Model model) {
        CustomerDetailResponse customer = customerService.get(customerId);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", CustomerEditForm.from(customer));
        }
        populateEditModel(model, customer);
        return "customers/edit";
    }

    @PostMapping("/{customerId}")
    String update(
            @PathVariable Long customerId,
            @Valid @ModelAttribute("form") CustomerEditForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateEditModel(model, customerService.get(customerId));
            return "customers/edit";
        }
        try {
            customerService.update(customerId, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Customer profile updated.");
            return "redirect:/ui/customers";
        } catch (BusinessRuleException | ResourceNotFoundException | OracleRoutineException exception) {
            model.addAttribute("error", exception.getMessage());
            populateEditModel(model, customerService.get(customerId));
            return "customers/edit";
        }
    }

    private void populateEditModel(Model model, CustomerDetailResponse customer) {
        model.addAttribute("customer", customer);
        model.addAttribute("activeNav", "customers");
    }
}
