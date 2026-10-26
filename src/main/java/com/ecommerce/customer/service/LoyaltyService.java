package com.ecommerce.customer.service;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.customer.api.dto.LoyaltyAccountResponse;
import com.ecommerce.customer.api.dto.LoyaltyPointsRequest;
import com.ecommerce.customer.api.dto.LoyaltyTransactionResponse;
import com.ecommerce.customer.domain.Customer;
import com.ecommerce.customer.domain.LoyaltyAccount;
import com.ecommerce.customer.domain.LoyaltyTier;
import com.ecommerce.customer.domain.LoyaltyTransaction;
import com.ecommerce.customer.domain.LoyaltyTransactionType;
import com.ecommerce.customer.gateway.LoyaltyTierGateway;
import com.ecommerce.customer.mapper.LoyaltyMapper;
import com.ecommerce.customer.repository.CustomerRepository;
import com.ecommerce.customer.repository.LoyaltyAccountRepository;
import com.ecommerce.customer.repository.LoyaltyTransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class LoyaltyService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> TRANSACTION_SORT_FIELDS = Set.of("id", "createdAt");

    private final CustomerRepository customerRepository;
    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final LoyaltyTierGateway loyaltyTierGateway;
    private final LoyaltyMapper loyaltyMapper;

    public LoyaltyService(
            CustomerRepository customerRepository,
            LoyaltyAccountRepository accountRepository,
            LoyaltyTransactionRepository transactionRepository,
            LoyaltyTierGateway loyaltyTierGateway,
            LoyaltyMapper loyaltyMapper) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.loyaltyTierGateway = loyaltyTierGateway;
        this.loyaltyMapper = loyaltyMapper;
    }

    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getAccount(Long customerId) {
        requireCustomer(customerId, false);
        return loyaltyMapper.toResponse(findAccount(customerId));
    }

    @Transactional
    public LoyaltyAccountResponse credit(Long customerId, LoyaltyPointsRequest request) {
        requireCustomer(customerId, true);
        String reference = normalizeReference(request.externalReference());
        LoyaltyAccount account = findAccountForUpdate(customerId);
        rejectDuplicateReference(customerId, reference);
        long balance = account.credit(request.points());
        recalculateTier(account);
        transactionRepository.save(new LoyaltyTransaction(
                account.getId(),
                customerId,
                LoyaltyTransactionType.CREDIT,
                request.points(),
                balance,
                reference,
                request.description()));
        return loyaltyMapper.toResponse(account);
    }

    @Transactional
    public LoyaltyAccountResponse redeem(Long customerId, LoyaltyPointsRequest request) {
        requireCustomer(customerId, true);
        String reference = normalizeReference(request.externalReference());
        LoyaltyAccount account = findAccountForUpdate(customerId);
        rejectDuplicateReference(customerId, reference);
        if (account.getPointsBalance() < request.points()) {
            throw new BusinessRuleException(
                    "INSUFFICIENT_LOYALTY_POINTS",
                    "Requested "
                            + request.points()
                            + " points, but the available balance is "
                            + account.getPointsBalance());
        }

        long balance = account.redeem(request.points());
        recalculateTier(account);
        transactionRepository.save(new LoyaltyTransaction(
                account.getId(),
                customerId,
                LoyaltyTransactionType.REDEMPTION,
                -request.points(),
                balance,
                reference,
                request.description()));
        return loyaltyMapper.toResponse(account);
    }

    @Transactional
    public LoyaltyAccountResponse recalculateTier(Long customerId) {
        requireCustomer(customerId, false);
        LoyaltyAccount account = findAccountForUpdate(customerId);
        recalculateTier(account);
        return loyaltyMapper.toResponse(account);
    }

    @Transactional(readOnly = true)
    public PageResponse<LoyaltyTransactionResponse> listTransactions(
            Long customerId,
            Pageable pageable) {
        requireCustomer(customerId, false);
        validatePageable(pageable);
        Page<LoyaltyTransaction> transactions =
                transactionRepository.findAllByCustomerId(customerId, pageable);
        return PageResponse.from(transactions, loyaltyMapper::toResponse);
    }

    private Customer requireCustomer(Long customerId, boolean mustBeActive) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        if (mustBeActive && !customer.isActive()) {
            throw new BusinessRuleException(
                    "CUSTOMER_INACTIVE",
                    "Loyalty points cannot be changed for an inactive customer");
        }
        return customer;
    }

    private LoyaltyAccount findAccount(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loyalty account for customer " + customerId + " was not found"));
    }

    private LoyaltyAccount findAccountForUpdate(Long customerId) {
        return accountRepository.findByCustomerIdForUpdate(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loyalty account for customer " + customerId + " was not found"));
    }

    private void rejectDuplicateReference(Long customerId, String externalReference) {
        if (transactionRepository.existsByCustomerIdAndExternalReference(
                customerId,
                externalReference)) {
            throw new BusinessRuleException(
                    "DUPLICATE_LOYALTY_REFERENCE",
                    "Loyalty reference " + externalReference + " has already been processed");
        }
    }

    private void recalculateTier(LoyaltyAccount account) {
        LoyaltyTier calculatedTier =
                loyaltyTierGateway.calculateTier(account.getPointsBalance());
        account.changeTier(calculatedTier);
    }

    private static String normalizeReference(String reference) {
        return reference.trim();
    }

    private static void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessRuleException(
                    "PAGE_SIZE_EXCEEDED",
                    "Page size may not exceed " + MAX_PAGE_SIZE);
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!TRANSACTION_SORT_FIELDS.contains(order.getProperty())) {
                throw new BusinessRuleException(
                        "INVALID_SORT",
                        "Sorting loyalty transactions by "
                                + order.getProperty()
                                + " is not supported");
            }
        }
    }
}
