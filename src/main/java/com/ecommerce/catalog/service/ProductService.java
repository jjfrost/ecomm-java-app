package com.ecommerce.catalog.service;

import com.ecommerce.catalog.api.dto.ProductAttributeRequest;
import com.ecommerce.catalog.api.dto.ProductAttributeResponse;
import com.ecommerce.catalog.api.dto.ProductAttributeValueRequest;
import com.ecommerce.catalog.api.dto.ProductAttributeValueResponse;
import com.ecommerce.catalog.api.dto.ProductCreateRequest;
import com.ecommerce.catalog.api.dto.ProductDetailResponse;
import com.ecommerce.catalog.api.dto.ProductImageRequest;
import com.ecommerce.catalog.api.dto.ProductImageResponse;
import com.ecommerce.catalog.api.dto.ProductSummaryResponse;
import com.ecommerce.catalog.api.dto.ProductUpdateRequest;
import com.ecommerce.catalog.domain.AttributeDataType;
import com.ecommerce.catalog.domain.Category;
import com.ecommerce.catalog.domain.Product;
import com.ecommerce.catalog.domain.ProductAttribute;
import com.ecommerce.catalog.domain.ProductAttributeValue;
import com.ecommerce.catalog.domain.ProductBundle;
import com.ecommerce.catalog.domain.ProductBundleItem;
import com.ecommerce.catalog.domain.ProductImage;
import com.ecommerce.catalog.mapper.CatalogMapper;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.ProductAttributeRepository;
import com.ecommerce.catalog.repository.ProductAttributeValueRepository;
import com.ecommerce.catalog.repository.ProductBundleItemRepository;
import com.ecommerce.catalog.repository.ProductBundleRepository;
import com.ecommerce.catalog.repository.ProductImageRepository;
import com.ecommerce.catalog.repository.ProductRepository;
import com.ecommerce.catalog.repository.ProductSpecifications;
import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_IMAGES = 12;
    private static final Set<String> PRODUCT_SORT_FIELDS =
            Set.of("id", "sku", "name", "price", "createdAt", "updatedAt");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAttributeRepository attributeRepository;
    private final ProductAttributeValueRepository valueRepository;
    private final ProductBundleRepository bundleRepository;
    private final ProductBundleItemRepository bundleItemRepository;
    private final CatalogMapper catalogMapper;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductImageRepository imageRepository,
            ProductAttributeRepository attributeRepository,
            ProductAttributeValueRepository valueRepository,
            ProductBundleRepository bundleRepository,
            ProductBundleItemRepository bundleItemRepository,
            CatalogMapper catalogMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.attributeRepository = attributeRepository;
        this.valueRepository = valueRepository;
        this.bundleRepository = bundleRepository;
        this.bundleItemRepository = bundleItemRepository;
        this.catalogMapper = catalogMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> search(
            String search,
            Long categoryId,
            Boolean active,
            BigDecimal minimumPrice,
            BigDecimal maximumPrice,
            Pageable pageable) {
        validatePageable(pageable);
        validatePriceRange(minimumPrice, maximumPrice);
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category", categoryId);
        }

        Page<Product> products = productRepository.findAll(
                ProductSpecifications.searchable(
                        search,
                        categoryId,
                        active,
                        minimumPrice,
                        maximumPrice),
                pageable);
        if (products.isEmpty()) {
            return PageResponse.from(products, product -> {
                throw new IllegalStateException("An empty page cannot be mapped");
            });
        }

        Set<Long> categoryIds = products.getContent().stream()
                .map(Product::getCategoryId)
                .collect(Collectors.toSet());
        Map<Long, Category> categories = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        List<Long> productIds = products.getContent().stream().map(Product::getId).toList();
        Map<Long, ProductImage> primaryImages = imageRepository.findAllByProductIdIn(productIds)
                .stream()
                .filter(ProductImage::isPrimaryImage)
                .collect(Collectors.toMap(
                        ProductImage::getProductId,
                        Function.identity(),
                        (first, ignored) -> first));

        return PageResponse.from(products, product -> catalogMapper.toSummary(
                product,
                requireCategory(categories, product.getCategoryId()),
                primaryImages.get(product.getId())));
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse get(Long productId) {
        return toDetail(findProduct(productId));
    }

    @Transactional
    public ProductDetailResponse create(ProductCreateRequest request) {
        String sku = request.sku().trim().toUpperCase(Locale.ROOT);
        if (productRepository.existsBySkuIgnoreCase(sku)) {
            throw new BusinessRuleException(
                    "PRODUCT_SKU_IN_USE",
                    "A product with SKU " + sku + " already exists");
        }
        requireCategory(request.categoryId());
        validateMoney(request.price(), "Product price");

        Product product = new Product(
                sku,
                request.name(),
                request.description(),
                request.price(),
                request.categoryId());
        return toDetail(productRepository.save(product));
    }

    @Transactional
    public ProductDetailResponse update(Long productId, ProductUpdateRequest request) {
        Product product = findProduct(productId);
        requireCategory(request.categoryId());
        validateMoney(request.price(), "Product price");
        if (product.getPrice().compareTo(request.price()) != 0) {
            validateActiveBundlePrices(productId, request.price());
        }

        product.updateDetails(
                request.name(),
                request.description(),
                request.price(),
                request.categoryId());
        if (product.isActive()) {
            validateActivation(product);
        }
        return toDetail(product);
    }

    @Transactional
    public ProductDetailResponse activate(Long productId) {
        Product product = findProduct(productId);
        validateActivation(product);
        product.activate();
        return toDetail(product);
    }

    @Transactional
    public ProductDetailResponse deactivate(Long productId) {
        Product product = findProduct(productId);
        List<Long> activeBundleIds = bundleItemRepository.findAllByProductId(productId)
                .stream()
                .map(ProductBundleItem::getBundleId)
                .distinct()
                .filter(bundleId -> bundleRepository.findById(bundleId)
                        .map(ProductBundle::isActive)
                        .orElse(false))
                .toList();
        if (!activeBundleIds.isEmpty()) {
            throw new BusinessRuleException(
                    "PRODUCT_IN_ACTIVE_BUNDLE",
                    "Deactivate bundles " + activeBundleIds + " before deactivating this product");
        }
        product.deactivate();
        return toDetail(product);
    }

    @Transactional(readOnly = true)
    public List<ProductImageResponse> listImages(Long productId) {
        findProduct(productId);
        return imageRepository
                .findAllByProductIdOrderByPrimaryImageDescSortOrderAsc(productId)
                .stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductImageResponse addImage(Long productId, ProductImageRequest request) {
        findProduct(productId);
        validateImageUrl(request.url());
        long imageCount = imageRepository.countByProductId(productId);
        if (imageCount >= MAX_IMAGES) {
            throw new BusinessRuleException(
                    "PRODUCT_IMAGE_LIMIT",
                    "A product may have at most " + MAX_IMAGES + " images");
        }

        boolean primary = imageCount == 0 || request.primaryImage();
        if (primary) {
            clearPrimaryImage(productId, null);
        }
        ProductImage image = new ProductImage(
                productId,
                request.url(),
                request.altText(),
                request.sortOrder(),
                primary);
        return catalogMapper.toResponse(imageRepository.save(image));
    }

    @Transactional
    public ProductImageResponse updateImage(
            Long productId,
            Long imageId,
            ProductImageRequest request) {
        findProduct(productId);
        ProductImage image = findImage(productId, imageId);
        validateImageUrl(request.url());

        if (request.primaryImage()) {
            clearPrimaryImage(productId, imageId);
            image.makePrimary();
        } else if (image.isPrimaryImage() && imageRepository.countByProductId(productId) > 1) {
            throw new BusinessRuleException(
                    "PRIMARY_IMAGE_REQUIRED",
                    "Choose another primary image before clearing the current primary image");
        }
        image.update(request.url(), request.altText(), request.sortOrder());
        return catalogMapper.toResponse(image);
    }

    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        Product product = findProduct(productId);
        ProductImage image = findImage(productId, imageId);
        List<ProductImage> remaining = imageRepository
                .findAllByProductIdOrderByPrimaryImageDescSortOrderAsc(productId)
                .stream()
                .filter(candidate -> !candidate.getId().equals(imageId))
                .toList();
        if (product.isActive() && remaining.isEmpty()) {
            throw new BusinessRuleException(
                    "ACTIVE_PRODUCT_IMAGE_REQUIRED",
                    "An active product must retain at least one image");
        }
        imageRepository.delete(image);
        if (image.isPrimaryImage() && !remaining.isEmpty()) {
            remaining.get(0).makePrimary();
        }
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeResponse> listAttributeDefinitions() {
        return attributeRepository.findAllByOrderByNameAsc()
                .stream()
                .map(catalogMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductAttributeResponse createAttribute(ProductAttributeRequest request) {
        String name = request.name().trim();
        if (attributeRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException(
                    "ATTRIBUTE_NAME_IN_USE",
                    "A product attribute named " + name + " already exists");
        }
        if (request.required() && productRepository.countByActiveTrue() > 0) {
            throw new BusinessRuleException(
                    "REQUIRED_ATTRIBUTE_HAS_ACTIVE_PRODUCTS",
                    "Deactivate products before introducing a new required attribute");
        }
        ProductAttribute attribute =
                new ProductAttribute(name, request.dataType(), request.required());
        return catalogMapper.toResponse(attributeRepository.save(attribute));
    }

    @Transactional
    public ProductAttributeResponse updateAttribute(
            Long attributeId,
            ProductAttributeRequest request) {
        ProductAttribute attribute = findAttribute(attributeId);
        String name = request.name().trim();
        if (attributeRepository.existsByNameIgnoreCaseAndIdNot(name, attributeId)) {
            throw new BusinessRuleException(
                    "ATTRIBUTE_NAME_IN_USE",
                    "A product attribute named " + name + " already exists");
        }
        boolean hasValues = valueRepository.existsByAttributeId(attributeId);
        if (hasValues && attribute.getDataType() != request.dataType()) {
            throw new BusinessRuleException(
                    "ATTRIBUTE_TYPE_IN_USE",
                    "An attribute's data type cannot change after values have been assigned");
        }
        if (request.required()
                && !attribute.isRequired()
                && productRepository.countByActiveTrue() > 0) {
            throw new BusinessRuleException(
                    "REQUIRED_ATTRIBUTE_HAS_ACTIVE_PRODUCTS",
                    "Deactivate products before making an attribute required");
        }
        attribute.update(name, request.dataType(), request.required());
        return catalogMapper.toResponse(attribute);
    }

    @Transactional
    public void deleteAttribute(Long attributeId) {
        ProductAttribute attribute = findAttribute(attributeId);
        if (valueRepository.existsByAttributeId(attributeId)) {
            throw new BusinessRuleException(
                    "ATTRIBUTE_IN_USE",
                    "Delete product values before deleting this attribute");
        }
        attributeRepository.delete(attribute);
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeValueResponse> listAttributeValues(Long productId) {
        findProduct(productId);
        return loadAttributeValueResponses(productId);
    }

    @Transactional
    public ProductAttributeValueResponse setAttributeValue(
            Long productId,
            Long attributeId,
            ProductAttributeValueRequest request) {
        findProduct(productId);
        ProductAttribute attribute = findAttribute(attributeId);
        String normalizedValue = validateAttributeValue(attribute, request.value());
        ProductAttributeValue value = valueRepository
                .findByProductIdAndAttributeId(productId, attributeId)
                .orElseGet(() -> new ProductAttributeValue(
                        productId,
                        attributeId,
                        normalizedValue));
        value.changeValue(normalizedValue);
        return catalogMapper.toResponse(valueRepository.save(value), attribute);
    }

    @Transactional
    public void deleteAttributeValue(Long productId, Long attributeId) {
        Product product = findProduct(productId);
        ProductAttribute attribute = findAttribute(attributeId);
        ProductAttributeValue value = valueRepository
                .findByProductIdAndAttributeId(productId, attributeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attribute "
                                + attributeId
                                + " value for product "
                                + productId
                                + " was not found"));
        if (product.isActive() && attribute.isRequired()) {
            throw new BusinessRuleException(
                    "ACTIVE_PRODUCT_REQUIRED_ATTRIBUTE",
                    "A required attribute cannot be removed from an active product");
        }
        valueRepository.delete(value);
    }

    private ProductDetailResponse toDetail(Product product) {
        return catalogMapper.toDetail(
                product,
                requireCategory(product.getCategoryId()),
                imageRepository.findAllByProductIdOrderByPrimaryImageDescSortOrderAsc(
                        product.getId()),
                loadAttributeValueResponses(product.getId()));
    }

    private List<ProductAttributeValueResponse> loadAttributeValueResponses(Long productId) {
        List<ProductAttributeValue> values = valueRepository.findAllByProductId(productId);
        Map<Long, ProductAttribute> attributes = attributeRepository.findAllById(
                        values.stream()
                                .map(ProductAttributeValue::getAttributeId)
                                .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(ProductAttribute::getId, Function.identity()));
        return values.stream()
                .map(value -> catalogMapper.toResponse(
                        value,
                        requireAttribute(attributes, value.getAttributeId())))
                .sorted(Comparator.comparing(ProductAttributeValueResponse::attributeName))
                .toList();
    }

    private void validateActivation(Product product) {
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new BusinessRuleException(
                    "PRODUCT_PRICE_INVALID",
                    "An active product must have a positive price");
        }
        requireCategory(product.getCategoryId());
        if (imageRepository.countByProductId(product.getId()) == 0
                || imageRepository.findFirstByProductIdAndPrimaryImageTrue(product.getId())
                        .isEmpty()) {
            throw new BusinessRuleException(
                    "PRODUCT_IMAGE_REQUIRED",
                    "An active product must have a primary image");
        }

        Set<Long> assignedAttributeIds = valueRepository.findAllByProductId(product.getId())
                .stream()
                .map(ProductAttributeValue::getAttributeId)
                .collect(Collectors.toSet());
        List<String> missingAttributes = attributeRepository
                .findAllByRequiredTrueOrderByNameAsc()
                .stream()
                .filter(attribute -> !assignedAttributeIds.contains(attribute.getId()))
                .map(ProductAttribute::getName)
                .toList();
        if (!missingAttributes.isEmpty()) {
            throw new BusinessRuleException(
                    "REQUIRED_PRODUCT_ATTRIBUTES_MISSING",
                    "Missing required attributes: " + missingAttributes);
        }
    }

    private void validateActiveBundlePrices(Long productId, BigDecimal proposedPrice) {
        Set<Long> bundleIds = bundleItemRepository.findAllByProductId(productId)
                .stream()
                .map(ProductBundleItem::getBundleId)
                .collect(Collectors.toSet());
        for (ProductBundle bundle : bundleRepository.findAllById(bundleIds)) {
            if (!bundle.isActive()) {
                continue;
            }
            List<ProductBundleItem> items =
                    bundleItemRepository.findAllByBundleIdOrderByIdAsc(bundle.getId());
            Map<Long, Product> products = productRepository.findAllById(
                            items.stream()
                                    .map(ProductBundleItem::getProductId)
                                    .collect(Collectors.toSet()))
                    .stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));
            BigDecimal componentTotal = items.stream()
                    .map(item -> {
                        BigDecimal unitPrice = item.getProductId().equals(productId)
                                ? proposedPrice
                                : requireProduct(products, item.getProductId()).getPrice();
                        return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (bundle.getPrice().compareTo(componentTotal) > 0) {
                throw new BusinessRuleException(
                        "BUNDLE_PRICE_EXCEEDS_COMPONENTS",
                        "The proposed price would make active bundle "
                                + bundle.getId()
                                + " cost more than its components");
            }
        }
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private Category requireCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    }

    private ProductImage findImage(Long productId, Long imageId) {
        return imageRepository.findByIdAndProductId(imageId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Image " + imageId + " for product " + productId + " was not found"));
    }

    private ProductAttribute findAttribute(Long attributeId) {
        return attributeRepository.findById(attributeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product attribute",
                        attributeId));
    }

    private void clearPrimaryImage(Long productId, Long excludedImageId) {
        imageRepository.findAllByProductIdOrderByPrimaryImageDescSortOrderAsc(productId)
                .stream()
                .filter(ProductImage::isPrimaryImage)
                .filter(image -> excludedImageId == null
                        || !image.getId().equals(excludedImageId))
                .forEach(ProductImage::clearPrimary);
    }

    private static String validateAttributeValue(
            ProductAttribute attribute,
            String rawValue) {
        String value = rawValue.trim();
        if (attribute.getDataType() == AttributeDataType.NUMBER) {
            try {
                new BigDecimal(value);
            } catch (NumberFormatException exception) {
                throw new BusinessRuleException(
                        "INVALID_ATTRIBUTE_VALUE",
                        attribute.getName() + " requires a numeric value");
            }
        } else if (attribute.getDataType() == AttributeDataType.BOOLEAN) {
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                throw new BusinessRuleException(
                        "INVALID_ATTRIBUTE_VALUE",
                        attribute.getName() + " requires true or false");
            }
            value = value.toLowerCase(Locale.ROOT);
        } else if (attribute.getDataType() == AttributeDataType.DATE) {
            try {
                LocalDate.parse(value);
            } catch (DateTimeParseException exception) {
                throw new BusinessRuleException(
                        "INVALID_ATTRIBUTE_VALUE",
                        attribute.getName() + " requires an ISO date");
            }
        }
        return value;
    }

    private static void validateImageUrl(String rawUrl) {
        try {
            URI uri = new URI(rawUrl.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new BusinessRuleException(
                        "INVALID_IMAGE_URL",
                        "Product image URL must be an absolute HTTPS URL");
            }
        } catch (URISyntaxException exception) {
            throw new BusinessRuleException(
                    "INVALID_IMAGE_URL",
                    "Product image URL is not a valid URI");
        }
    }

    private static void validatePriceRange(
            BigDecimal minimumPrice,
            BigDecimal maximumPrice) {
        if (minimumPrice != null && minimumPrice.signum() < 0
                || maximumPrice != null && maximumPrice.signum() < 0) {
            throw new BusinessRuleException(
                    "INVALID_PRICE_RANGE",
                    "Price filters cannot be negative");
        }
        if (minimumPrice != null
                && maximumPrice != null
                && minimumPrice.compareTo(maximumPrice) > 0) {
            throw new BusinessRuleException(
                    "INVALID_PRICE_RANGE",
                    "Minimum price cannot exceed maximum price");
        }
    }

    private static void validateMoney(BigDecimal amount, String fieldName) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException(
                    "INVALID_PRICE",
                    fieldName + " must be positive");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new BusinessRuleException(
                    "INVALID_PRICE_SCALE",
                    fieldName + " may have at most two decimal places");
        }
    }

    private static void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Page size may not exceed " + MAX_PAGE_SIZE);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!PRODUCT_SORT_FIELDS.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "INVALID_SORT",
                        "Sorting products by " + order.getProperty() + " is not supported");
            }
        }
    }

    private static Category requireCategory(
            Map<Long, Category> categories,
            Long categoryId) {
        Category category = categories.get(categoryId);
        if (category == null) {
            throw new IllegalStateException(
                    "Product references missing category " + categoryId);
        }
        return category;
    }

    private static ProductAttribute requireAttribute(
            Map<Long, ProductAttribute> attributes,
            Long attributeId) {
        ProductAttribute attribute = attributes.get(attributeId);
        if (attribute == null) {
            throw new IllegalStateException(
                    "Product value references missing attribute " + attributeId);
        }
        return attribute;
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
