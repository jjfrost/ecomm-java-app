package com.ecommerce.ui.web;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.oracle.OracleRoutineException;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.service.InventoryService;
import com.ecommerce.ui.form.InventoryAdjustmentForm;
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
@RequestMapping("/ui/inventory")
public class UiInventoryController {
    private final InventoryService inventoryService;

    public UiInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "false") boolean reorderOnly,
            Model model) {
        model.addAttribute(
                "page",
                inventoryService.list(
                        null,
                        location,
                        reorderOnly,
                        PageRequest.of(page, 10, Sort.by("location"))));
        model.addAttribute("location", location);
        model.addAttribute("reorderOnly", reorderOnly);
        model.addAttribute("activeNav", "inventory");
        return "inventory/list";
    }

    @GetMapping("/{inventoryId}/adjust")
    String edit(
            @PathVariable Long inventoryId,
            Model model) {
        InventoryResponse inventory = inventoryService.get(inventoryId);
        if (!model.containsAttribute("form")) {
            InventoryAdjustmentForm form = new InventoryAdjustmentForm();
            form.setPerformedBy("demo-operator");
            model.addAttribute("form", form);
        }
        populateEditModel(model, inventory);
        return "inventory/adjust";
    }

    @PostMapping("/{inventoryId}/adjust")
    String adjust(
            @PathVariable Long inventoryId,
            @Valid @ModelAttribute("form") InventoryAdjustmentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateEditModel(model, inventoryService.get(inventoryId));
            return "inventory/adjust";
        }
        try {
            inventoryService.adjust(inventoryId, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Inventory quantity adjusted.");
            return "redirect:/ui/inventory";
        } catch (BusinessRuleException | ResourceNotFoundException | OracleRoutineException exception) {
            model.addAttribute("error", exception.getMessage());
            populateEditModel(model, inventoryService.get(inventoryId));
            return "inventory/adjust";
        }
    }

    private void populateEditModel(Model model, InventoryResponse inventory) {
        model.addAttribute("inventory", inventory);
        model.addAttribute("activeNav", "inventory");
    }
}
