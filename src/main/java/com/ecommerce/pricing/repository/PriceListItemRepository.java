package com.ecommerce.pricing.repository;

import com.ecommerce.pricing.domain.PriceListItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PriceListItemRepository extends JpaRepository<PriceListItem, Long> {
    Page<PriceListItem> findByPriceListId(Long priceListId, Pageable pageable);
    boolean existsByPriceListId(Long priceListId);
    boolean existsByPriceListIdAndProductId(Long priceListId, Long productId);
    boolean existsByPriceListIdAndProductIdAndPriceListItemIdNot(
            Long priceListId, Long productId, Long priceListItemId);

    @Query("""
            select item
              from PriceListItem item
              join PriceList priceList on priceList.priceListId = item.priceListId
             where item.productId = :productId
               and item.minimumQuantity <= :quantity
               and item.validFrom <= :asOf
               and (item.validTo is null or item.validTo > :asOf)
               and priceList.active = true
               and priceList.validFrom <= :asOf
               and (priceList.validTo is null or priceList.validTo > :asOf)
             order by priceList.priority desc, item.minimumQuantity desc
            """)
    List<PriceListItem> findEffectiveCandidates(
            @Param("productId") Long productId,
            @Param("quantity") BigDecimal quantity,
            @Param("asOf") Instant asOf,
            Pageable pageable);

    @Query(value = """
            SELECT item.*
              FROM ecomm.price_list_items item
              JOIN ecomm.price_lists pl
                ON pl.price_list_id = item.price_list_id
             WHERE item.product_id = :productId
               AND item.minimum_quantity <= :quantity
               AND pl.currency_code = UPPER(:currencyCode)
               AND DECODE(pl.is_active, 'Y', 1, 0) = 1
               AND TRUNC(SYSDATE)
                   BETWEEN TRUNC(pl.valid_from)
                       AND TRUNC(NVL(pl.valid_to, SYSDATE))
               AND TRUNC(SYSDATE)
                   BETWEEN TRUNC(item.valid_from)
                       AND TRUNC(NVL(item.valid_to, SYSDATE))
             ORDER BY pl.priority DESC, item.minimum_quantity DESC, item.price
             FETCH FIRST 1 ROW ONLY
            """, nativeQuery = true)
    Optional<PriceListItem> findOracleDailyEffectivePrice(
            @Param("productId") Long productId,
            @Param("quantity") BigDecimal quantity,
            @Param("currencyCode") String currencyCode);
}
