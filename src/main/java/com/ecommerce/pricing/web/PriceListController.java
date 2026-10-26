package com.ecommerce.pricing.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.pricing.dto.PriceListItemRequest;
import com.ecommerce.pricing.dto.PriceListItemResponse;
import com.ecommerce.pricing.dto.PriceListRequest;
import com.ecommerce.pricing.dto.PriceListResponse;
import com.ecommerce.pricing.service.PricingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/price-lists")
public class PriceListController {
    private final PricingService pricingService;

    public PriceListController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public PageResponse<PriceListResponse> list(
            @RequestParam(defaultValue = "false") boolean activeOnly,
            Pageable pageable) {
        return pricingService.listPriceLists(activeOnly, pageable);
    }

    @GetMapping("/{priceListId}")
    public PriceListResponse get(@PathVariable @Positive Long priceListId) {
        return pricingService.getPriceList(priceListId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<PriceListResponse> create(
            @Valid @RequestBody PriceListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pricingService.createPriceList(request));
    }

    @PutMapping("/{priceListId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public PriceListResponse update(
            @PathVariable @Positive Long priceListId,
            @Valid @RequestBody PriceListRequest request) {
        return pricingService.updatePriceList(priceListId, request);
    }

    @DeleteMapping("/{priceListId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long priceListId) {
        pricingService.deletePriceList(priceListId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{priceListId}/items")
    public PageResponse<PriceListItemResponse> listItems(
            @PathVariable @Positive Long priceListId,
            Pageable pageable) {
        return pricingService.listItems(priceListId, pageable);
    }

    @PostMapping("/{priceListId}/items")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<PriceListItemResponse> addItem(
            @PathVariable @Positive Long priceListId,
            @Valid @RequestBody PriceListItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pricingService.addItem(priceListId, request));
    }

    @PutMapping("/{priceListId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public PriceListItemResponse updateItem(
            @PathVariable @Positive Long priceListId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody PriceListItemRequest request) {
        return pricingService.updateItem(priceListId, itemId, request);
    }

    @DeleteMapping("/{priceListId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('MERCHANDISER', 'ADMIN')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable @Positive Long priceListId,
            @PathVariable @Positive Long itemId) {
        pricingService.deleteItem(priceListId, itemId);
        return ResponseEntity.noContent().build();
    }
}
