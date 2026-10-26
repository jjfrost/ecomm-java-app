package com.ecommerce.cart.repository;

import com.ecommerce.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartIdOrderByCartItemId(Long cartId);
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
    Optional<CartItem> findByCartIdAndCartItemId(Long cartId, Long cartItemId);
    boolean existsByCartId(Long cartId);
    void deleteByCartId(Long cartId);
}
