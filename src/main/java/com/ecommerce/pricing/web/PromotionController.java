package com.ecommerce.pricing.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.pricing.dto.CouponRequest;
import com.ecommerce.pricing.dto.CouponResponse;
import com.ecommerce.pricing.dto.PromotionRequest;
import com.ecommerce.pricing.dto.PromotionResponse;
import com.ecommerce.pricing.dto.PromotionRuleRequest;
import com.ecommerce.pricing.dto.PromotionRuleResponse;
import com.ecommerce.pricing.service.PromotionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public PageResponse<PromotionResponse> list(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            Pageable pageable) {
        return promotionService.listPromotions(activeOnly, pageable);
    }

    @GetMapping("/{promotionId}")
    public PromotionResponse get(@PathVariable @Positive Long promotionId) {
        return promotionService.getPromotion(promotionId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<PromotionResponse> create(
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promotionService.createPromotion(request));
    }

    @PutMapping("/{promotionId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public PromotionResponse update(
            @PathVariable @Positive Long promotionId,
            @Valid @RequestBody PromotionRequest request) {
        return promotionService.updatePromotion(promotionId, request);
    }

    @DeleteMapping("/{promotionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long promotionId) {
        promotionService.deletePromotion(promotionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{promotionId}/rules")
    public List<PromotionRuleResponse> listRules(@PathVariable @Positive Long promotionId) {
        return promotionService.listRules(promotionId);
    }

    @PostMapping("/{promotionId}/rules")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<PromotionRuleResponse> addRule(
            @PathVariable @Positive Long promotionId,
            @Valid @RequestBody PromotionRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promotionService.addRule(promotionId, request));
    }

    @PutMapping("/{promotionId}/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public PromotionRuleResponse updateRule(
            @PathVariable @Positive Long promotionId,
            @PathVariable @Positive Long ruleId,
            @Valid @RequestBody PromotionRuleRequest request) {
        return promotionService.updateRule(promotionId, ruleId, request);
    }

    @DeleteMapping("/{promotionId}/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<Void> deleteRule(
            @PathVariable @Positive Long promotionId,
            @PathVariable @Positive Long ruleId) {
        promotionService.deleteRule(promotionId, ruleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{promotionId}/coupons")
    public PageResponse<CouponResponse> listCoupons(
            @PathVariable @Positive Long promotionId,
            Pageable pageable) {
        return promotionService.listCoupons(promotionId, pageable);
    }

    @PostMapping("/coupons")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CouponRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promotionService.createCoupon(request));
    }

    @PutMapping("/coupons/{couponId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public CouponResponse updateCoupon(
            @PathVariable @Positive Long couponId,
            @Valid @RequestBody CouponRequest request) {
        return promotionService.updateCoupon(couponId, request);
    }

    @DeleteMapping("/coupons/{couponId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<Void> deleteCoupon(@PathVariable @Positive Long couponId) {
        promotionService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }
}
