package com.ecommerce.pricing.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.pricing.domain.Coupon;
import com.ecommerce.pricing.domain.DiscountType;
import com.ecommerce.pricing.domain.Promotion;
import com.ecommerce.pricing.domain.PromotionRule;
import com.ecommerce.pricing.dto.CouponRequest;
import com.ecommerce.pricing.dto.CouponResponse;
import com.ecommerce.pricing.dto.CouponValidationRequest;
import com.ecommerce.pricing.dto.CouponValidationResponse;
import com.ecommerce.pricing.dto.PromotionRequest;
import com.ecommerce.pricing.dto.PromotionResponse;
import com.ecommerce.pricing.dto.PromotionRuleRequest;
import com.ecommerce.pricing.dto.PromotionRuleResponse;
import com.ecommerce.pricing.repository.CouponRepository;
import com.ecommerce.pricing.repository.PromotionRepository;
import com.ecommerce.pricing.repository.PromotionRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;
    private final PromotionRuleRepository ruleRepository;
    private final CouponRepository couponRepository;
    private final PricingEngineGateway pricingEngine;
    private final PricingMapper mapper;

    public PromotionService(
            PromotionRepository promotionRepository,
            PromotionRuleRepository ruleRepository,
            CouponRepository couponRepository,
            PricingEngineGateway pricingEngine,
            PricingMapper mapper) {
        this.promotionRepository = promotionRepository;
        this.ruleRepository = ruleRepository;
        this.couponRepository = couponRepository;
        this.pricingEngine = pricingEngine;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<PromotionResponse> listPromotions(boolean activeOnly, Pageable pageable) {
        Page<Promotion> page = activeOnly
                ? promotionRepository.findByActiveTrue(pageable)
                : promotionRepository.findAll(pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotion(Long promotionId) {
        return mapper.toResponse(requirePromotion(promotionId));
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        validatePromotion(request, null);
        if (promotionRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessRuleException("PROMOTION_NAME_EXISTS", "Promotion name is already in use");
        }
        return mapper.toResponse(promotionRepository.save(mapper.toEntity(request)));
    }

    @Transactional
    public PromotionResponse updatePromotion(Long promotionId, PromotionRequest request) {
        Promotion promotion = requirePromotion(promotionId);
        validatePromotion(request, promotion);
        if (promotionRepository.existsByNameIgnoreCaseAndPromotionIdNot(
                request.name().trim(), promotionId)) {
            throw new BusinessRuleException("PROMOTION_NAME_EXISTS", "Promotion name is already in use");
        }
        mapper.update(promotion, request);
        return mapper.toResponse(promotionRepository.save(promotion));
    }

    @Transactional
    public void deletePromotion(Long promotionId) {
        Promotion promotion = requirePromotion(promotionId);
        if (ruleRepository.existsByPromotionId(promotionId)
                || couponRepository.existsByPromotionId(promotionId)) {
            throw new BusinessRuleException(
                    "PROMOTION_HAS_DEPENDENCIES",
                    "A promotion with rules or coupons cannot be deleted");
        }
        promotionRepository.delete(promotion);
    }

    @Transactional(readOnly = true)
    public List<PromotionRuleResponse> listRules(Long promotionId) {
        requirePromotion(promotionId);
        return ruleRepository.findByPromotionIdOrderByPromotionRuleId(promotionId)
                .stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PromotionRuleResponse addRule(Long promotionId, PromotionRuleRequest request) {
        requirePromotion(promotionId);
        return mapper.toResponse(ruleRepository.save(mapper.toEntity(promotionId, request)));
    }

    @Transactional
    public PromotionRuleResponse updateRule(
            Long promotionId,
            Long ruleId,
            PromotionRuleRequest request) {
        PromotionRule rule = requireRule(ruleId);
        assertRuleBelongsToPromotion(rule, promotionId);
        rule.applyChanges(request.ruleType(), request.operator(), request.value().trim());
        return mapper.toResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long promotionId, Long ruleId) {
        PromotionRule rule = requireRule(ruleId);
        assertRuleBelongsToPromotion(rule, promotionId);
        ruleRepository.delete(rule);
    }

    @Transactional(readOnly = true)
    public PageResponse<CouponResponse> listCoupons(Long promotionId, Pageable pageable) {
        requirePromotion(promotionId);
        return PageResponse.from(couponRepository.findByPromotionId(promotionId, pageable), mapper::toResponse);
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest request) {
        Promotion promotion = requirePromotion(request.promotionId());
        validateCouponWindow(request, promotion, null);
        String code = mapper.normalizeCoupon(request.code());
        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessRuleException("COUPON_CODE_EXISTS", "Coupon code is already in use");
        }
        return mapper.toResponse(couponRepository.save(mapper.toEntity(request)));
    }

    @Transactional
    public CouponResponse updateCoupon(Long couponId, CouponRequest request) {
        Coupon coupon = requireCoupon(couponId);
        Promotion promotion = requirePromotion(request.promotionId());
        validateCouponWindow(request, promotion, coupon);
        String code = mapper.normalizeCoupon(request.code());
        if (couponRepository.existsByCodeIgnoreCaseAndCouponIdNot(code, couponId)) {
            throw new BusinessRuleException("COUPON_CODE_EXISTS", "Coupon code is already in use");
        }
        mapper.update(coupon, request);
        return mapper.toResponse(couponRepository.save(coupon));
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        couponRepository.delete(requireCoupon(couponId));
    }

    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(CouponValidationRequest request) {
        String code = mapper.normalizeCoupon(request.code());
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code).orElse(null);
        if (coupon == null) {
            return new CouponValidationResponse(code, null, false, "Coupon does not exist");
        }
        Instant asOf = request.asOf() == null ? Instant.now() : request.asOf();
        if (!coupon.isAvailableAt(asOf)) {
            return new CouponValidationResponse(code, coupon.getPromotionId(), false, "Coupon is inactive or expired");
        }
        Promotion promotion = requirePromotion(coupon.getPromotionId());
        if (!promotion.isAvailableAt(asOf)) {
            return new CouponValidationResponse(code, coupon.getPromotionId(), false, "Promotion is unavailable");
        }
        boolean valid = pricingEngine.validateCoupon(
                code, request.customerId(), request.subtotal(), asOf);
        return new CouponValidationResponse(
                code,
                coupon.getPromotionId(),
                valid,
                valid ? "Coupon is valid" : "Coupon was rejected by the pricing package");
    }

    private void validatePromotion(PromotionRequest request, Promotion current) {
        if (!request.validTo().isAfter(request.validFrom())) {
            throw new BusinessRuleException(
                    "INVALID_PROMOTION_WINDOW",
                    "Promotion end must be after its start");
        }
        if (request.discountType() == DiscountType.PERCENTAGE
                && request.discountValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw new BusinessRuleException(
                    "INVALID_PERCENTAGE_DISCOUNT",
                    "Percentage discounts cannot exceed 100");
        }
        if (current != null
                && request.usageLimit() != null
                && request.usageLimit() < current.getUsageCount()) {
            throw new BusinessRuleException(
                    "PROMOTION_USAGE_LIMIT_BELOW_USAGE",
                    "Usage limit cannot be less than current usage");
        }
    }

    private void validateCouponWindow(CouponRequest request, Promotion promotion, Coupon current) {
        if (!request.validTo().isAfter(request.validFrom())) {
            throw new BusinessRuleException(
                    "INVALID_COUPON_WINDOW",
                    "Coupon end must be after its start");
        }
        if (request.validFrom().isBefore(promotion.getValidFrom())
                || request.validTo().isAfter(promotion.getValidTo())) {
            throw new BusinessRuleException(
                    "COUPON_OUTSIDE_PROMOTION_WINDOW",
                    "Coupon validity must be contained within its promotion");
        }
        if (current != null
                && request.usageLimit() != null
                && request.usageLimit() < current.getUsageCount()) {
            throw new BusinessRuleException(
                    "COUPON_USAGE_LIMIT_BELOW_USAGE",
                    "Usage limit cannot be less than current usage");
        }
    }

    private Promotion requirePromotion(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion", promotionId));
    }

    private PromotionRule requireRule(Long ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("PromotionRule", ruleId));
    }

    private Coupon requireCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", couponId));
    }

    private void assertRuleBelongsToPromotion(PromotionRule rule, Long promotionId) {
        if (!rule.getPromotionId().equals(promotionId)) {
            throw new ResourceNotFoundException("PromotionRule", rule.getPromotionRuleId());
        }
    }
}
