package com.ecommerce.pricing.web;

import com.ecommerce.pricing.dto.CouponValidationRequest;
import com.ecommerce.pricing.dto.CouponValidationResponse;
import com.ecommerce.pricing.dto.EffectivePriceResponse;
import com.ecommerce.pricing.service.PricingService;
import com.ecommerce.pricing.service.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/pricing")
public class PricingController {
    private final PricingService pricingService;
    private final PromotionService promotionService;

    public PricingController(PricingService pricingService, PromotionService promotionService) {
        this.pricingService = pricingService;
        this.promotionService = promotionService;
    }

    @GetMapping("/effective")
    public EffectivePriceResponse quote(
            @RequestParam @Positive Long productId,
            @RequestParam(defaultValue = "1") @DecimalMin("0.01") BigDecimal quantity,
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) Instant asOf) {
        return pricingService.quote(productId, quantity, customerId, couponCode, asOf);
    }

    @PostMapping("/coupons/validate")
    public CouponValidationResponse validateCoupon(
            @Valid @RequestBody CouponValidationRequest request) {
        return promotionService.validateCoupon(request);
    }
}
