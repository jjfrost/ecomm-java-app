package com.ecommerce.catalog.service;

import com.ecommerce.catalog.api.dto.ProductBundleItemRequest;
import com.ecommerce.catalog.api.dto.ProductBundleRequest;
import com.ecommerce.catalog.api.dto.ProductBundleResponse;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.ProductBundle;
import com.ecommerce.catalog.domain.ProductBundleItem;
import com.ecommerce.catalog.mapper.BundleMapper;
import com.ecommerce.catalog.repository.ProductBundleItemRepository;
import com.ecommerce.catalog.repository.ProductBundleRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BundleService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> BUNDLE_SORT_FIELDS =
            Set.of("id", "name", "price", "createdAt", "updatedAt");

    private final ProductBundleRepository bundleRepository;
    private final ProductBundleItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final BundleMapper bundleMapper;

    public BundleService(
            ProductBundleRepository bundleRepository,
            ProductBundleItemRepository itemRepository,
            ProductRepository productRepository,
            BundleMapper bundleMapper) {
        this.bundleRepository = bundleRepository;
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.bundleMapper = bundleMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductBundleResponse> list(Pageable pageable) {
        validatePageable(pageable);
        Page<ProductBundle> bundles = bundleRepository.findAll(pageable);
        return PageResponse.from(bundles, this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductBundleResponse get(Long bundleId) {
        return toResponse(findBundle(bundleId));
    }

    @Transactional
    public ProductBundleResponse create(ProductBundleRequest request) {
        String name = request.name().trim();
        if (bundleRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException(
                    "BUNDLE_NAME_IN_USE",
                    "A bundle named " + name + " already exists");
        }
        Map<Long, Product> products = validateComposition(request.items(), true);
        validateBundlePrice(request.price(), request.items(), products);

        ProductBundle bundle = bundleRepository.saveAndFlush(
                new ProductBundle(name, request.description(), request.price()));
        List<ProductBundleItem> items = request.items().stream()
                .map(item -> new ProductBundleItem(
                        bundle.getId(),
                        item.productId(),
                        item.quantity()))
                .toList();
        items = itemRepository.saveAll(items);
        return bundleMapper.toResponse(bundle, items, products);
    }

    @Transactional
    public ProductBundleResponse update(
            Long bundleId,
            ProductBundleRequest request) {
        ProductBundle bundle = findBundle(bundleId);
        String name = request.name().trim();
        if (bundleRepository.existsByNameIgnoreCaseAndIdNot(name, bundleId)) {
            throw new BusinessRuleException(
                    "BUNDLE_NAME_IN_USE",
                    "A bundle named " + name + " already exists");
        }
        Map<Long, Product> products =
                validateComposition(request.items(), bundle.isActive());
        validateBundlePrice(request.price(), request.items(), products);

        bundle.update(name, request.description(), request.price());
        itemRepository.deleteAllByBundleId(bundleId);
        List<ProductBundleItem> items = itemRepository.saveAll(request.items().stream()
                .map(item -> new ProductBundleItem(
                        bundleId,
                        item.productId(),
                        item.quantity()))
                .toList());
        return bundleMapper.toResponse(bundle, items, products);
    }

    @Transactional
    public ProductBundleResponse activate(Long bundleId) {
        ProductBundle bundle = findBundle(bundleId);
        List<ProductBundleItem> items =
                itemRepository.findAllByBundleIdOrderByIdAsc(bundleId);
        List<ProductBundleItemRequest> itemRequests = items.stream()
                .map(item -> new ProductBundleItemRequest(
                        item.getProductId(),
                        item.getQuantity()))
                .toList();
        Map<Long, Product> products = validateComposition(itemRequests, true);
        validateBundlePrice(bundle.getPrice(), itemRequests, products);
        bundle.activate();
        return bundleMapper.toResponse(bundle, items, products);
    }

    @Transactional
    public ProductBundleResponse deactivate(Long bundleId) {
        ProductBundle bundle = findBundle(bundleId);
        bundle.deactivate();
        return toResponse(bundle);
    }

    @Transactional
    public void delete(Long bundleId) {
        ProductBundle bundle = findBundle(bundleId);
        if (bundle.isActive()) {
            throw new BusinessRuleException(
                    "ACTIVE_BUNDLE_DELETE",
                    "Deactivate a bundle before deleting it");
        }
        itemRepository.deleteAllByBundleId(bundleId);
        bundleRepository.delete(bundle);
    }

    private ProductBundleResponse toResponse(ProductBundle bundle) {
        List<ProductBundleItem> items =
                itemRepository.findAllByBundleIdOrderByIdAsc(bundle.getId());
        Map<Long, Product> products = productRepository.findAllById(
                        items.stream()
                                .map(ProductBundleItem::getProductId)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return bundleMapper.toResponse(bundle, items, products);
    }

    private Map<Long, Product> validateComposition(
            List<ProductBundleItemRequest> items,
            boolean requireActiveProducts) {
        if (items == null || items.size() < 2 || items.size() > 25) {
            throw new BusinessRuleException(
                    "BUNDLE_ITEM_COUNT",
                    "A bundle must contain between 2 and 25 distinct products");
        }
        Set<Long> productIds =
                items.stream().map(ProductBundleItemRequest::productId).collect(Collectors.toSet());
        if (productIds.size() != items.size()) {
            throw new BusinessRuleException(
                    "DUPLICATE_BUNDLE_PRODUCT",
                    "A product may appear only once in a bundle; use quantity instead");
        }
        if (items.stream().anyMatch(item -> item.quantity() < 1 || item.quantity() > 99)) {
            throw new BusinessRuleException(
                    "INVALID_BUNDLE_QUANTITY",
                    "Bundle quantities must be between 1 and 99");
        }

        Map<Long, Product> products = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        Set<Long> missingProductIds = productIds.stream()
                .filter(productId -> !products.containsKey(productId))
                .collect(Collectors.toSet());
        if (!missingProductIds.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Bundle products were not found: " + missingProductIds);
        }
        if (requireActiveProducts) {
            List<Long> inactiveProductIds = products.values().stream()
                    .filter(product -> !product.isActive())
                    .map(Product::getId)
                    .toList();
            if (!inactiveProductIds.isEmpty()) {
                throw new BusinessRuleException(
                        "INACTIVE_BUNDLE_PRODUCT",
                        "Bundles may contain only active products: " + inactiveProductIds);
            }
        }
        return products;
    }

    private static void validateBundlePrice(
            BigDecimal bundlePrice,
            List<ProductBundleItemRequest> items,
            Map<Long, Product> products) {
        if (bundlePrice == null || bundlePrice.signum() <= 0) {
            throw new BusinessRuleException(
                    "INVALID_BUNDLE_PRICE",
                    "Bundle price must be positive");
        }
        if (bundlePrice.stripTrailingZeros().scale() > 2) {
            throw new BusinessRuleException(
                    "INVALID_BUNDLE_PRICE_SCALE",
                    "Bundle price may have at most two decimal places");
        }
        BigDecimal componentTotal = items.stream()
                .map(item -> products.get(item.productId())
                        .getPrice()
                        .multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (bundlePrice.compareTo(componentTotal) >= 0) {
            throw new BusinessRuleException(
                    "BUNDLE_PRICE_NOT_DISCOUNTED",
                    "Bundle price must be lower than component total " + componentTotal);
        }
    }

    private ProductBundle findBundle(Long bundleId) {
        return bundleRepository.findById(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Product bundle", bundleId));
    }

    private static void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Page size may not exceed " + MAX_PAGE_SIZE);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!BUNDLE_SORT_FIELDS.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "INVALID_SORT",
                        "Sorting bundles by " + order.getProperty() + " is not supported");
            }
        }
    }
}
