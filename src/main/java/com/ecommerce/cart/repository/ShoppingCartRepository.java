package com.ecommerce.cart.repository;

import com.ecommerce.cart.domain.CartStatus;
import com.ecommerce.cart.domain.ShoppingCart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    Optional<ShoppingCart> findByCustomerIdAndStatus(Long customerId, CartStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ShoppingCart c where c.cartId = :cartId")
    Optional<ShoppingCart> findLockedById(@Param("cartId") Long cartId);
}
