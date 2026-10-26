package com.ecommerce.ui.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.ecommerce.ui.web")
public class UiModelAdvice {
    @ModelAttribute("currentUser")
    String currentUser() {
        return "Demo Operator";
    }
}
