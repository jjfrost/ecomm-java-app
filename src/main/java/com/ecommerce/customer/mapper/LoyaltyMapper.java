package com.ecommerce.customer.mapper;

import com.ecommerce.customer.api.dto.LoyaltyAccountResponse;
import com.ecommerce.customer.api.dto.LoyaltyTransactionResponse;
import com.ecommerce.customer.domain.LoyaltyAccount;
import com.ecommerce.customer.domain.LoyaltyTransaction;
import org.springframework.stereotype.Component;

@Component
public class LoyaltyMapper {
    public LoyaltyAccountResponse toResponse(LoyaltyAccount account) {
        return new LoyaltyAccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getPointsBalance(),
                account.getTier(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    public LoyaltyTransactionResponse toResponse(LoyaltyTransaction transaction) {
        return new LoyaltyTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getPointsDelta(),
                transaction.getBalanceAfter(),
                transaction.getExternalReference(),
                transaction.getDescription(),
                transaction.getCreatedAt());
    }
}
