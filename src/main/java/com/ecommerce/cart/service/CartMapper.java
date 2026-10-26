package com.ecommerce.cart.service;

import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.domain.ShoppingCart;
import com.ecommerce.cart.dto.CartItemResponse;
import com.ecommerce.cart.dto.CartResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CartMapper {
    public CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
                item.getCartItemId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitDiscount(),
                item.subtotal(),
                item.discountTotal(),
                item.total());
    }

    public CartResponse toResponse(ShoppingCart cart, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream().map(this::toResponse).toList();
        BigDecimal subtotal = items.stream()
                .map(CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = items.stream()
                .map(CartItem::discountTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(
                cart.getCartId(),
                cart.getCustomerId(),
                cart.getStatus(),
                cart.getCouponCode(),
                itemResponses,
                subtotal,
                discount,
                subtotal.subtract(discount).max(BigDecimal.ZERO),
                cart.getExpiresAt(),
                cart.getCreatedAt(),
                cart.getUpdatedAt());
    }
}
