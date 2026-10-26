package com.ecommerce.customer.service;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.customer.api.dto.LoyaltyAccountResponse;
import com.ecommerce.customer.api.dto.LoyaltyPointsRequest;
import com.ecommerce.customer.domain.Customer;
import com.ecommerce.customer.domain.HomeAddress;
import com.ecommerce.customer.domain.LoyaltyAccount;
import com.ecommerce.customer.domain.LoyaltyTier;
import com.ecommerce.customer.domain.LoyaltyTransaction;
import com.ecommerce.customer.domain.LoyaltyTransactionType;
import com.ecommerce.customer.gateway.LoyaltyTierGateway;
import com.ecommerce.customer.mapper.LoyaltyMapper;
import com.ecommerce.customer.repository.CustomerRepository;
import com.ecommerce.customer.repository.LoyaltyAccountRepository;
import com.ecommerce.customer.repository.LoyaltyTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoyaltyAccountRepository accountRepository;

    @Mock
    private LoyaltyTransactionRepository transactionRepository;

    @Mock
    private LoyaltyTierGateway loyaltyTierGateway;

    private LoyaltyService loyaltyService;

    @BeforeEach
    void setUp() {
        loyaltyService = new LoyaltyService(
                customerRepository,
                accountRepository,
                transactionRepository,
                loyaltyTierGateway,
                new LoyaltyMapper());
    }

    @Test
    void creditLocksAccountRecalculatesTierAndWritesLedgerEntry() {
        Customer customer = activeCustomer();
        LoyaltyAccount account = new LoyaltyAccount(7L);
        ReflectionTestUtils.setField(account, "id", 12L);
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerIdForUpdate(7L)).thenReturn(Optional.of(account));
        when(transactionRepository.existsByCustomerIdAndExternalReference(
                7L,
                "ORDER-42"))
                .thenReturn(false);
        when(loyaltyTierGateway.calculateTier(250L)).thenReturn(LoyaltyTier.SILVER);
        when(transactionRepository.save(any(LoyaltyTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LoyaltyAccountResponse response = loyaltyService.credit(
                7L,
                new LoyaltyPointsRequest(250L, " ORDER-42 ", "Purchase reward"));

        ArgumentCaptor<LoyaltyTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LoyaltyTransaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        LoyaltyTransaction transaction = transactionCaptor.getValue();
        assertEquals(250L, response.pointsBalance());
        assertEquals(LoyaltyTier.SILVER, response.tier());
        assertEquals(LoyaltyTransactionType.CREDIT, transaction.getType());
        assertEquals(250L, transaction.getPointsDelta());
        assertEquals(250L, transaction.getBalanceAfter());
        assertEquals("ORDER-42", transaction.getExternalReference());
        verify(loyaltyTierGateway).calculateTier(250L);
    }

    @Test
    void redemptionRejectsInsufficientBalanceWithoutLedgerOrTierCall() {
        Customer customer = activeCustomer();
        LoyaltyAccount account = new LoyaltyAccount(7L);
        account.credit(20L);
        ReflectionTestUtils.setField(account, "id", 12L);
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
        when(accountRepository.findByCustomerIdForUpdate(7L)).thenReturn(Optional.of(account));
        when(transactionRepository.existsByCustomerIdAndExternalReference(
                7L,
                "REWARD-9"))
                .thenReturn(false);

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> loyaltyService.redeem(
                        7L,
                        new LoyaltyPointsRequest(25L, "REWARD-9", "Reward")));

        assertEquals("INSUFFICIENT_LOYALTY_POINTS", exception.getRuleCode());
        assertEquals(20L, account.getPointsBalance());
        verify(transactionRepository, never()).save(any(LoyaltyTransaction.class));
        verifyNoInteractions(loyaltyTierGateway);
    }

    private static Customer activeCustomer() {
        return new Customer(
                "Katherine Johnson",
                "katherine@example.com",
                new HomeAddress("1 Orbit Drive", "Hampton", "23666"));
    }
}
