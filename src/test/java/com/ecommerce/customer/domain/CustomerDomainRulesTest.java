package com.ecommerce.customer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerDomainRulesTest {
    @Test
    void customerNormalizesProfileAndCanBeDeactivated() {
        Customer customer = new Customer(
                " Ada Lovelace ",
                " ADA@Example.COM ",
                new HomeAddress(" 12 Computing Lane ", " London ", " SW1A 1AA "));

        assertEquals("Ada Lovelace", customer.getName());
        assertEquals("ada@example.com", customer.getEmail());
        assertEquals("12 Computing Lane", customer.getHomeAddress().getStreet());
        assertTrue(customer.isActive());

        customer.deactivate();

        assertFalse(customer.isActive());
    }

    @Test
    void loyaltyAccountMaintainsBalanceAndCalculatedTier() {
        LoyaltyAccount account = new LoyaltyAccount(42L);

        assertEquals(500L, account.credit(500L));
        assertEquals(375L, account.redeem(125L));

        account.changeTier(LoyaltyTier.GOLD);

        assertEquals(375L, account.getPointsBalance());
        assertEquals(LoyaltyTier.GOLD, account.getTier());
    }
}
