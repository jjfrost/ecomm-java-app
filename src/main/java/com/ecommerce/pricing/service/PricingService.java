package com.ecommerce.pricing.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.pricing.domain.PriceList;
import com.ecommerce.pricing.domain.PriceListItem;
import com.ecommerce.pricing.dto.EffectivePriceResponse;
import com.ecommerce.pricing.dto.PriceListItemRequest;
import com.ecommerce.pricing.dto.PriceListItemResponse;
import com.ecommerce.pricing.dto.PriceListRequest;
import com.ecommerce.pricing.dto.PriceListResponse;
import com.ecommerce.pricing.repository.PriceListItemRepository;
import com.ecommerce.pricing.repository.PriceListRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class PricingService {
    private final PriceListRepository priceListRepository;
    private final PriceListItemRepository itemRepository;
    private final PricingEngineGateway pricingEngine;
    private final PricingMapper mapper;

    public PricingService(
            PriceListRepository priceListRepository,
            PriceListItemRepository itemRepository,
            PricingEngineGateway pricingEngine,
            PricingMapper mapper) {
        this.priceListRepository = priceListRepository;
        this.itemRepository = itemRepository;
        this.pricingEngine = pricingEngine;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<PriceListResponse> listPriceLists(boolean activeOnly, Pageable pageable) {
        Page<PriceList> page = activeOnly
                ? priceListRepository.findByActiveTrue(pageable)
                : priceListRepository.findAll(pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PriceListResponse getPriceList(Long priceListId) {
        return mapper.toResponse(requirePriceList(priceListId));
    }

    @Transactional
    public PriceListResponse createPriceList(PriceListRequest request) {
        validateWindow(request.validFrom(), request.validTo());
        if (priceListRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BusinessRuleException("PRICE_LIST_NAME_EXISTS", "Price list name is already in use");
        }
        return mapper.toResponse(priceListRepository.save(mapper.toEntity(request)));
    }

    @Transactional
    public PriceListResponse updatePriceList(Long priceListId, PriceListRequest request) {
        validateWindow(request.validFrom(), request.validTo());
        PriceList priceList = requirePriceList(priceListId);
        if (priceListRepository.existsByNameIgnoreCaseAndPriceListIdNot(
                request.name().trim(), priceListId)) {
            throw new BusinessRuleException("PRICE_LIST_NAME_EXISTS", "Price list name is already in use");
        }
        mapper.update(priceList, request);
        return mapper.toResponse(priceListRepository.save(priceList));
    }

    @Transactional
    public void deletePriceList(Long priceListId) {
        PriceList priceList = requirePriceList(priceListId);
        if (itemRepository.existsByPriceListId(priceListId)) {
            throw new BusinessRuleException(
                    "PRICE_LIST_HAS_ITEMS",
                    "A price list with items cannot be deleted");
        }
        priceListRepository.delete(priceList);
    }

    @Transactional(readOnly = true)
    public PageResponse<PriceListItemResponse> listItems(Long priceListId, Pageable pageable) {
        requirePriceList(priceListId);
        return PageResponse.from(itemRepository.findByPriceListId(priceListId, pageable), mapper::toResponse);
    }

    @Transactional
    public PriceListItemResponse addItem(Long priceListId, PriceListItemRequest request) {
        PriceList priceList = requirePriceList(priceListId);
        validateItem(request, priceList);
        if (itemRepository.existsByPriceListIdAndProductId(priceListId, request.productId())) {
            throw new BusinessRuleException(
                    "PRICE_LIST_PRODUCT_EXISTS",
                    "The product already has an item in this price list");
        }
        return mapper.toResponse(itemRepository.save(mapper.toEntity(priceListId, request)));
    }

    @Transactional
    public PriceListItemResponse updateItem(
            Long priceListId,
            Long itemId,
            PriceListItemRequest request) {
        PriceList priceList = requirePriceList(priceListId);
        PriceListItem item = requireItem(itemId);
        assertItemBelongsToList(item, priceListId);
        validateItem(request, priceList);
        if (itemRepository.existsByPriceListIdAndProductIdAndPriceListItemIdNot(
                priceListId, request.productId(), itemId)) {
            throw new BusinessRuleException(
                    "PRICE_LIST_PRODUCT_EXISTS",
                    "The product already has an item in this price list");
        }
        mapper.update(item, request);
        return mapper.toResponse(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long priceListId, Long itemId) {
        PriceListItem item = requireItem(itemId);
        assertItemBelongsToList(item, priceListId);
        itemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public EffectivePriceResponse quote(
            Long productId,
            BigDecimal quantity,
            Long customerId,
            String couponCode,
            Instant requestedAt) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new BusinessRuleException(
                    "INVALID_PRICING_QUANTITY",
                    "Pricing quantity must be greater than zero");
        }
        Instant asOf = requestedAt == null ? Instant.now() : requestedAt;
        List<PriceListItem> candidates = itemRepository.findEffectiveCandidates(
                productId, quantity, asOf, PageRequest.of(0, 1));
        if (candidates.isEmpty()) {
            throw new ResourceNotFoundException("No effective price was found for product " + productId);
        }
        PriceListItem baseItem = candidates.get(0);
        PriceList priceList = requirePriceList(baseItem.getPriceListId());
        String normalizedCoupon = couponCode == null || couponCode.isBlank()
                ? null
                : mapper.normalizeCoupon(couponCode);
        BigDecimal effectivePrice = pricingEngine.getEffectivePrice(
                productId, quantity, customerId, normalizedCoupon, asOf);
        if (effectivePrice == null
                || effectivePrice.signum() < 0
                || effectivePrice.compareTo(baseItem.getPrice()) > 0) {
            throw new BusinessRuleException(
                    "INVALID_EFFECTIVE_PRICE",
                    "Pricing package returned an invalid effective price");
        }
        return new EffectivePriceResponse(
                productId,
                quantity,
                priceList.getPriceListId(),
                priceList.getCurrencyCode(),
                baseItem.getPrice(),
                effectivePrice,
                baseItem.getPrice().subtract(effectivePrice),
                normalizedCoupon,
                asOf);
    }

    private void validateItem(PriceListItemRequest request, PriceList priceList) {
        validateWindow(request.validFrom(), request.validTo());
        if (request.validFrom().isBefore(priceList.getValidFrom())
                || (priceList.getValidTo() != null
                && (request.validTo() == null || request.validTo().isAfter(priceList.getValidTo())))) {
            throw new BusinessRuleException(
                    "PRICE_ITEM_OUTSIDE_LIST_WINDOW",
                    "Price item validity must be contained within the price list validity");
        }
    }

    private void validateWindow(Instant validFrom, Instant validTo) {
        if (validTo != null && !validTo.isAfter(validFrom)) {
            throw new BusinessRuleException(
                    "INVALID_VALIDITY_WINDOW",
                    "Validity end must be after validity start");
        }
    }

    private void assertItemBelongsToList(PriceListItem item, Long priceListId) {
        if (!item.getPriceListId().equals(priceListId)) {
            throw new ResourceNotFoundException("PriceListItem", item.getPriceListItemId());
        }
    }

    private PriceList requirePriceList(Long priceListId) {
        return priceListRepository.findById(priceListId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceList", priceListId));
    }

    private PriceListItem requireItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceListItem", itemId));
    }
}
