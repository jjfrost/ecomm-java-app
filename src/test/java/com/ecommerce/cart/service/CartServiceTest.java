package com.ecommerce.cart.service;

import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.domain.ShoppingCart;
import com.ecommerce.cart.dto.ApplyCouponRequest;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.ShoppingCartRepository;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.pricing.dto.CouponValidationRequest;
import com.ecommerce.pricing.dto.CouponValidationResponse;
import com.ecommerce.pricing.service.PricingService;
import com.ecommerce.pricing.service.PromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private CartItemRepository itemRepository;

    @Mock
    private PricingService pricingService;

    @Mock
    private PromotionService promotionService;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(
                cartRepository,
                itemRepository,
                pricingService,
                promotionService,
                new CartMapper());
    }

    @Test
    void invalidCouponStopsBeforeAnyCartRepricing() {
        ShoppingCart cart = cart();
        CartItem item = CartItem.create(
                5L,
                99L,
                new BigDecimal("2.00"),
                new BigDecimal("10.00"),
                BigDecimal.ZERO);
        when(cartRepository.findLockedById(5L)).thenReturn(Optional.of(cart));
        when(itemRepository.findByCartIdOrderByCartItemId(5L)).thenReturn(List.of(item));
        when(promotionService.validateCoupon(any(CouponValidationRequest.class)))
                .thenReturn(new CouponValidationResponse(
                        "BAD",
                        null,
                        false,
                        "Coupon does not exist"));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> cartService.applyCoupon(5L, new ApplyCouponRequest("bad")));

        assertAll(
                () -> assertEquals("INVALID_COUPON", exception.getRuleCode()),
                () -> assertNull(cart.getCouponCode()));
        verifyNoInteractions(pricingService);
    }

    private static ShoppingCart cart() {
        ShoppingCart cart = ShoppingCart.create(
                42L,
                Instant.now().plus(1, ChronoUnit.HOURS));
        ReflectionTestUtils.setField(cart, "cartId", 5L);
        return cart;
    }
}
