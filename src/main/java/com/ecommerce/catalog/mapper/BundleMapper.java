package com.ecommerce.catalog.mapper;

import com.ecommerce.catalog.api.dto.ProductBundleItemResponse;
import com.ecommerce.catalog.api.dto.ProductBundleResponse;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.ProductBundle;
import com.ecommerce.catalog.domain.ProductBundleItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class BundleMapper {
    public ProductBundleResponse toResponse(
            ProductBundle bundle,
            List<ProductBundleItem> items,
            Map<Long, Product> products) {
        List<ProductBundleItemResponse> itemResponses = items.stream()
                .map(item -> toItemResponse(item, requireProduct(products, item.getProductId())))
                .toList();
        BigDecimal componentTotal = itemResponses.stream()
                .map(ProductBundleItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ProductBundleResponse(
                bundle.getId(),
                bundle.getName(),
                bundle.getDescription(),
                bundle.getPrice(),
                componentTotal,
                componentTotal.subtract(bundle.getPrice()),
                bundle.isActive(),
                bundle.getCreatedAt(),
                bundle.getUpdatedAt(),
                itemResponses);
    }

    private ProductBundleItemResponse toItemResponse(
            ProductBundleItem item,
            Product product) {
        BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new ProductBundleItemResponse(
                item.getId(),
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                lineTotal);
    }

    private static Product requireProduct(Map<Long, Product> products, Long productId) {
        Product product = products.get(productId);
        if (product == null) {
            throw new IllegalStateException(
                    "Bundle references missing product " + productId);
        }
        return product;
    }
}
