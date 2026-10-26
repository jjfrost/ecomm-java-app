package com.ecommerce.pricing.service;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.pricing.domain.PriceList;
import com.ecommerce.pricing.domain.PriceListItem;
import com.ecommerce.pricing.dto.EffectivePriceResponse;
import com.ecommerce.pricing.repository.PriceListItemRepository;
import com.ecommerce.pricing.repository.PriceListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {
    private static final Instant PRICED_AT = Instant.parse("2026-10-04T20:00:00Z");

    @Mock
    private PriceListRepository priceListRepository;

    @Mock
    private PriceListItemRepository itemRepository;

    @Mock
    private PricingEngineGateway pricingEngine;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(
                priceListRepository,
                itemRepository,
                pricingEngine,
                new PricingMapper());
    }

    @Test
    void quoteSelectsCandidateNormalizesCouponAndReturnsUnitDiscount() {
        stubCandidate(new BigDecimal("8.00"));

        EffectivePriceResponse response = pricingService.quote(
                7L,
                new BigDecimal("2.00"),
                55L,
                " save10 ",
                PRICED_AT);

        assertAll(
                () -> assertEquals(10L, response.priceListId()),
                () -> assertEquals("USD", response.currencyCode()),
                () -> assertEquals(new BigDecimal("10.00"), response.basePrice()),
                () -> assertEquals(new BigDecimal("8.00"), response.effectiveUnitPrice()),
                () -> assertEquals(new BigDecimal("2.00"), response.unitDiscount()),
                () -> assertEquals("SAVE10", response.couponCode()));
        verify(pricingEngine).getEffectivePrice(
                7L,
                new BigDecimal("2.00"),
                55L,
                "SAVE10",
                PRICED_AT);
    }

    @Test
    void quoteRejectsPriceAboveConfiguredBasePrice() {
        stubCandidate(new BigDecimal("10.01"));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> pricingService.quote(
                        7L,
                        new BigDecimal("2.00"),
                        55L,
                        null,
                        PRICED_AT));

        assertEquals("INVALID_EFFECTIVE_PRICE", exception.getRuleCode());
    }

    private void stubCandidate(BigDecimal effectivePrice) {
        PriceList priceList = PriceList.create(
                "Retail",
                "USD",
                PRICED_AT.minusSeconds(3600),
                PRICED_AT.plusSeconds(3600),
                true,
                100);
        ReflectionTestUtils.setField(priceList, "priceListId", 10L);
        PriceListItem item = PriceListItem.create(
                10L,
                7L,
                new BigDecimal("10.00"),
                BigDecimal.ONE,
                PRICED_AT.minusSeconds(3600),
                PRICED_AT.plusSeconds(3600));
        when(itemRepository.findEffectiveCandidates(
                eq(7L),
                eq(new BigDecimal("2.00")),
                eq(PRICED_AT),
                any(Pageable.class)))
                .thenReturn(List.of(item));
        when(priceListRepository.findById(10L)).thenReturn(Optional.of(priceList));
        when(pricingEngine.getEffectivePrice(
                eq(7L),
                eq(new BigDecimal("2.00")),
                eq(55L),
                any(),
                eq(PRICED_AT)))
                .thenReturn(effectivePrice);
    }
}
