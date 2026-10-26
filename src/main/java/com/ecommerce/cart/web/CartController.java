package com.ecommerce.cart.web;

import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.ApplyCouponRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.CreateCartRequest;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<CartResponse> create(@Valid @RequestBody CreateCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.create(request));
    }

    @GetMapping("/{cartId}")
    public CartResponse get(@PathVariable @Positive Long cartId) {
        return cartService.get(cartId);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable @Positive Long cartId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(cartId, request));
    }

    @PutMapping("/{cartId}/items/{cartItemId}")
    public CartResponse updateItem(
            @PathVariable @Positive Long cartId,
            @PathVariable @Positive Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(cartId, cartItemId, request);
    }

    @DeleteMapping("/{cartId}/items/{cartItemId}")
    public CartResponse removeItem(
            @PathVariable @Positive Long cartId,
            @PathVariable @Positive Long cartItemId) {
        return cartService.removeItem(cartId, cartItemId);
    }

    @PutMapping("/{cartId}/coupon")
    public CartResponse applyCoupon(
            @PathVariable @Positive Long cartId,
            @Valid @RequestBody ApplyCouponRequest request) {
        return cartService.applyCoupon(cartId, request);
    }

    @DeleteMapping("/{cartId}/coupon")
    public CartResponse removeCoupon(@PathVariable @Positive Long cartId) {
        return cartService.removeCoupon(cartId);
    }

    @PostMapping("/{cartId}/abandon")
    public CartResponse abandon(@PathVariable @Positive Long cartId) {
        return cartService.abandon(cartId);
    }
}
