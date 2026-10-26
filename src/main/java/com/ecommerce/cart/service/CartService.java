package com.ecommerce.cart.service;

import com.ecommerce.cart.domain.CartItem;
import com.ecommerce.cart.domain.CartStatus;
import com.ecommerce.cart.domain.ShoppingCart;
import com.ecommerce.cart.dto.AddCartItemRequest;
import com.ecommerce.cart.dto.ApplyCouponRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.dto.CreateCartRequest;
import com.ecommerce.cart.dto.UpdateCartItemRequest;
import com.ecommerce.cart.repository.CartItemRepository;
import com.ecommerce.cart.repository.ShoppingCartRepository;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.pricing.dto.CouponValidationRequest;
import com.ecommerce.pricing.dto.CouponValidationResponse;
import com.ecommerce.pricing.dto.EffectivePriceResponse;
import com.ecommerce.pricing.service.PricingService;
import com.ecommerce.pricing.service.PromotionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class CartService {
    private static final Duration CART_LIFETIME = Duration.ofDays(30);
    private static final BigDecimal MAX_LINE_QUANTITY = new BigDecimal("10000.00");

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final PricingService pricingService;
    private final PromotionService promotionService;
    private final CartMapper mapper;

    public CartService(
            ShoppingCartRepository cartRepository,
            CartItemRepository itemRepository,
            PricingService pricingService,
            PromotionService promotionService,
            CartMapper mapper) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.pricingService = pricingService;
        this.promotionService = promotionService;
        this.mapper = mapper;
    }

    @Transactional
    public CartResponse create(CreateCartRequest request) {
        if (cartRepository.findByCustomerIdAndStatus(request.customerId(), CartStatus.ACTIVE).isPresent()) {
            throw new BusinessRuleException(
                    "ACTIVE_CART_EXISTS",
                    "The customer already has an active cart");
        }
        ShoppingCart cart = ShoppingCart.create(
                request.customerId(),
                Instant.now().plus(CART_LIFETIME));
        return mapper.toResponse(cartRepository.save(cart), List.of());
    }

    @Transactional(readOnly = true)
    public CartResponse get(Long cartId) {
        ShoppingCart cart = requireCart(cartId);
        return mapper.toResponse(cart, itemRepository.findByCartIdOrderByCartItemId(cartId));
    }

    @Transactional
    public CartResponse addItem(Long cartId, AddCartItemRequest request) {
        ShoppingCart cart = requireMutableCart(cartId);
        CartItem existing = itemRepository.findByCartIdAndProductId(cartId, request.productId()).orElse(null);
        BigDecimal quantity = existing == null
                ? request.quantity()
                : existing.getQuantity().add(request.quantity());
        validateQuantity(quantity);
        EffectivePriceResponse price = quote(cart, request.productId(), quantity);
        if (existing == null) {
            itemRepository.save(CartItem.create(
                    cartId, request.productId(), quantity,
                    price.basePrice(), price.unitDiscount()));
        } else {
            existing.reprice(quantity, price.basePrice(), price.unitDiscount());
            itemRepository.save(existing);
        }
        return currentResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(
            Long cartId,
            Long cartItemId,
            UpdateCartItemRequest request) {
        ShoppingCart cart = requireMutableCart(cartId);
        validateQuantity(request.quantity());
        CartItem item = requireItem(cartId, cartItemId);
        EffectivePriceResponse price = quote(cart, item.getProductId(), request.quantity());
        item.reprice(request.quantity(), price.basePrice(), price.unitDiscount());
        itemRepository.save(item);
        return currentResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long cartId, Long cartItemId) {
        ShoppingCart cart = requireMutableCart(cartId);
        itemRepository.delete(requireItem(cartId, cartItemId));
        return currentResponse(cart);
    }

    @Transactional
    public CartResponse applyCoupon(Long cartId, ApplyCouponRequest request) {
        ShoppingCart cart = requireMutableCart(cartId);
        List<CartItem> items = itemRepository.findByCartIdOrderByCartItemId(cartId);
        if (items.isEmpty()) {
            throw new BusinessRuleException(
                    "EMPTY_CART_COUPON",
                    "A coupon cannot be applied to an empty cart");
        }
        BigDecimal subtotal = items.stream()
                .map(CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CouponValidationResponse validation = promotionService.validateCoupon(
                new CouponValidationRequest(
                        request.couponCode(),
                        cart.getCustomerId(),
                        subtotal,
                        Instant.now()));
        if (!validation.valid()) {
            throw new BusinessRuleException("INVALID_COUPON", validation.reason());
        }
        cart.applyCoupon(validation.code());
        repriceItems(cart, items);
        return mapper.toResponse(cart, items);
    }

    @Transactional
    public CartResponse removeCoupon(Long cartId) {
        ShoppingCart cart = requireMutableCart(cartId);
        cart.removeCoupon();
        List<CartItem> items = itemRepository.findByCartIdOrderByCartItemId(cartId);
        repriceItems(cart, items);
        return mapper.toResponse(cart, items);
    }

    @Transactional
    public CartResponse abandon(Long cartId) {
        ShoppingCart cart = requireMutableCart(cartId);
        cart.abandon();
        return currentResponse(cart);
    }

    @Transactional
    public CartResponse checkoutSnapshot(Long cartId) {
        ShoppingCart cart = requireMutableCart(cartId);
        List<CartItem> items = itemRepository.findByCartIdOrderByCartItemId(cartId);
        if (items.isEmpty()) {
            throw new BusinessRuleException("EMPTY_CART", "An empty cart cannot be submitted");
        }
        repriceItems(cart, items);
        return mapper.toResponse(cart, items);
    }

    @Transactional
    public void markCheckedOut(Long cartId) {
        ShoppingCart cart = requireMutableCart(cartId);
        if (!itemRepository.existsByCartId(cartId)) {
            throw new BusinessRuleException("EMPTY_CART", "An empty cart cannot be checked out");
        }
        cart.markCheckedOut();
    }

    private void repriceItems(ShoppingCart cart, List<CartItem> items) {
        for (CartItem item : items) {
            EffectivePriceResponse price = quote(cart, item.getProductId(), item.getQuantity());
            item.reprice(item.getQuantity(), price.basePrice(), price.unitDiscount());
            itemRepository.save(item);
        }
    }

    private EffectivePriceResponse quote(
            ShoppingCart cart,
            Long productId,
            BigDecimal quantity) {
        return pricingService.quote(
                productId,
                quantity,
                cart.getCustomerId(),
                cart.getCouponCode(),
                Instant.now());
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0 || quantity.compareTo(MAX_LINE_QUANTITY) > 0) {
            throw new BusinessRuleException(
                    "INVALID_CART_QUANTITY",
                    "Cart item quantity must be greater than zero and no more than 10000");
        }
    }

    private CartResponse currentResponse(ShoppingCart cart) {
        return mapper.toResponse(cart, itemRepository.findByCartIdOrderByCartItemId(cart.getCartId()));
    }

    private ShoppingCart requireCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart", cartId));
    }

    private ShoppingCart requireMutableCart(Long cartId) {
        ShoppingCart cart = cartRepository.findLockedById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingCart", cartId));
        cart.assertMutable(Instant.now());
        return cart;
    }

    private CartItem requireItem(Long cartId, Long cartItemId) {
        return itemRepository.findByCartIdAndCartItemId(cartId, cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemId));
    }
}
