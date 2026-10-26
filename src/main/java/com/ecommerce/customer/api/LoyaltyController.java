package com.ecommerce.customer.api;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.customer.api.dto.LoyaltyAccountResponse;
import com.ecommerce.customer.api.dto.LoyaltyPointsRequest;
import com.ecommerce.customer.api.dto.LoyaltyTransactionResponse;
import com.ecommerce.customer.service.LoyaltyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/customers/{customerId}/loyalty")
public class LoyaltyController {
    private final LoyaltyService loyaltyService;

    public LoyaltyController(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @GetMapping
    public LoyaltyAccountResponse getAccount(
            @PathVariable @Positive Long customerId) {
        return loyaltyService.getAccount(customerId);
    }

    @PostMapping("/credits")
    public LoyaltyAccountResponse credit(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody LoyaltyPointsRequest request) {
        return loyaltyService.credit(customerId, request);
    }

    @PostMapping("/redemptions")
    public LoyaltyAccountResponse redeem(
            @PathVariable @Positive Long customerId,
            @Valid @RequestBody LoyaltyPointsRequest request) {
        return loyaltyService.redeem(customerId, request);
    }

    @PostMapping("/tier/recalculate")
    public LoyaltyAccountResponse recalculateTier(
            @PathVariable @Positive Long customerId) {
        return loyaltyService.recalculateTier(customerId);
    }

    @GetMapping("/transactions")
    public PageResponse<LoyaltyTransactionResponse> listTransactions(
            @PathVariable @Positive Long customerId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return loyaltyService.listTransactions(customerId, pageable);
    }
}
