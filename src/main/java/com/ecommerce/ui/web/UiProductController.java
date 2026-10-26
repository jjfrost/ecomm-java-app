package com.ecommerce.ui.web;

import com.ecommerce.catalog.api.dto.ProductDetailResponse;
import com.ecommerce.catalog.service.CategoryService;
import com.ecommerce.catalog.service.ProductService;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.ui.form.ProductEditForm;
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
@RequestMapping("/ui/products")
public class UiProductController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public UiProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String search,
            Model model) {
        model.addAttribute(
                "page",
                productService.search(
                        search,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(page, 10, Sort.by("name"))));
        model.addAttribute("search", search);
        model.addAttribute("activeNav", "products");
        return "products/list";
    }

    @GetMapping("/{productId}/edit")
    String edit(@PathVariable Long productId, Model model) {
        ProductDetailResponse product = productService.get(productId);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", ProductEditForm.from(product));
        }
        populateEditModel(model, product);
        return "products/edit";
    }

    @PostMapping("/{productId}")
    String update(
            @PathVariable Long productId,
            @Valid @ModelAttribute("form") ProductEditForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            populateEditModel(model, productService.get(productId));
            return "products/edit";
        }
        try {
            productService.update(productId, form.toRequest());
            redirectAttributes.addFlashAttribute("success", "Product details updated.");
            return "redirect:/ui/products";
        } catch (BusinessRuleException | ResourceNotFoundException exception) {
            model.addAttribute("error", exception.getMessage());
            populateEditModel(model, productService.get(productId));
            return "products/edit";
        }
    }

    private void populateEditModel(Model model, ProductDetailResponse product) {
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.list());
        model.addAttribute("activeNav", "products");
    }
}
