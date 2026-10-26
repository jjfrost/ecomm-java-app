package com.ecommerce.pricing.service;

import com.ecommerce.pricing.domain.Coupon;
import com.ecommerce.pricing.domain.PriceList;
import com.ecommerce.pricing.domain.PriceListItem;
import com.ecommerce.pricing.domain.Promotion;
import com.ecommerce.pricing.domain.PromotionRule;
import com.ecommerce.pricing.dto.CouponRequest;
import com.ecommerce.pricing.dto.CouponResponse;
import com.ecommerce.pricing.dto.PriceListItemRequest;
import com.ecommerce.pricing.dto.PriceListItemResponse;
import com.ecommerce.pricing.dto.PriceListRequest;
import com.ecommerce.pricing.dto.PriceListResponse;
import com.ecommerce.pricing.dto.PromotionRequest;
import com.ecommerce.pricing.dto.PromotionResponse;
import com.ecommerce.pricing.dto.PromotionRuleRequest;
import com.ecommerce.pricing.dto.PromotionRuleResponse;
import org.springframework.stereotype.Component;

@Component
public class PricingMapper {
    public PriceList toEntity(PriceListRequest request) {
        return PriceList.create(
                request.name().trim(),
                request.currencyCode().trim().toUpperCase(),
                request.validFrom(),
                request.validTo(),
                request.active(),
                request.priority());
    }

    public void update(PriceList priceList, PriceListRequest request) {
        priceList.applyChanges(
                request.name().trim(),
                request.currencyCode().trim().toUpperCase(),
                request.validFrom(),
                request.validTo(),
                request.active(),
                request.priority());
    }

    public PriceListResponse toResponse(PriceList priceList) {
        return new PriceListResponse(
                priceList.getPriceListId(), priceList.getName(), priceList.getCurrencyCode(),
                priceList.getValidFrom(), priceList.getValidTo(), Boolean.TRUE.equals(priceList.getActive()),
                priceList.getPriority(), priceList.getCreatedAt(), priceList.getUpdatedAt());
    }

    public PriceListItem toEntity(Long priceListId, PriceListItemRequest request) {
        return PriceListItem.create(
                priceListId, request.productId(), request.price(), request.minimumQuantity(),
                request.validFrom(), request.validTo());
    }

    public void update(PriceListItem item, PriceListItemRequest request) {
        item.applyChanges(
                request.productId(), request.price(), request.minimumQuantity(),
                request.validFrom(), request.validTo());
    }

    public PriceListItemResponse toResponse(PriceListItem item) {
        return new PriceListItemResponse(
                item.getPriceListItemId(), item.getPriceListId(), item.getProductId(), item.getPrice(),
                item.getMinimumQuantity(), item.getValidFrom(), item.getValidTo(),
                item.getCreatedAt(), item.getUpdatedAt());
    }

    public Promotion toEntity(PromotionRequest request) {
        return Promotion.create(
                request.name().trim(), trimToNull(request.description()), request.discountType(),
                request.discountValue(), request.validFrom(), request.validTo(), request.active(),
                request.priority(), request.usageLimit());
    }

    public void update(Promotion promotion, PromotionRequest request) {
        promotion.applyChanges(
                request.name().trim(), trimToNull(request.description()), request.discountType(),
                request.discountValue(), request.validFrom(), request.validTo(), request.active(),
                request.priority(), request.usageLimit());
    }

    public PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getPromotionId(), promotion.getName(), promotion.getDescription(),
                promotion.getDiscountType(), promotion.getDiscountValue(), promotion.getValidFrom(),
                promotion.getValidTo(), Boolean.TRUE.equals(promotion.getActive()), promotion.getPriority(),
                promotion.getUsageLimit(), promotion.getUsageCount(),
                promotion.getCreatedAt(), promotion.getUpdatedAt());
    }

    public PromotionRule toEntity(Long promotionId, PromotionRuleRequest request) {
        return PromotionRule.create(
                promotionId, request.ruleType(), request.operator(), request.value().trim());
    }

    public PromotionRuleResponse toResponse(PromotionRule rule) {
        return new PromotionRuleResponse(
                rule.getPromotionRuleId(), rule.getPromotionId(), rule.getRuleType(),
                rule.getOperator(), rule.getValue(), rule.getCreatedAt());
    }

    public Coupon toEntity(CouponRequest request) {
        return Coupon.create(
                normalizeCoupon(request.code()), request.promotionId(), request.validFrom(), request.validTo(),
                request.usageLimit(), request.perCustomerLimit(), request.active());
    }

    public void update(Coupon coupon, CouponRequest request) {
        coupon.applyChanges(
                normalizeCoupon(request.code()), request.promotionId(), request.validFrom(), request.validTo(),
                request.usageLimit(), request.perCustomerLimit(), request.active());
    }

    public CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getCouponId(), coupon.getCode(), coupon.getPromotionId(),
                coupon.getValidFrom(), coupon.getValidTo(), coupon.getUsageLimit(), coupon.getUsageCount(),
                coupon.getPerCustomerLimit(), Boolean.TRUE.equals(coupon.getActive()),
                coupon.getCreatedAt(), coupon.getUpdatedAt());
    }

    public String normalizeCoupon(String code) {
        return code.trim().toUpperCase();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
