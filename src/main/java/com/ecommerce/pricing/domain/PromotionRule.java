package com.ecommerce.pricing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "promotion_rules", schema = "ecomm")
public class PromotionRule {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "promotion_rule_generator")
    @SequenceGenerator(
            name = "promotion_rule_generator",
            sequenceName = "ecomm.promotion_rule_seq",
            allocationSize = 1)
    @Column(name = "promotion_rule_id")
    private Long promotionRuleId;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 30)
    private PromotionRuleType ruleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_operator", nullable = false, length = 30)
    private RuleOperator operator;

    @Column(name = "rule_value", nullable = false, length = 500)
    private String value;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PromotionRule() {
    }

    public static PromotionRule create(
            Long promotionId,
            PromotionRuleType ruleType,
            RuleOperator operator,
            String value) {
        PromotionRule rule = new PromotionRule();
        rule.promotionId = promotionId;
        rule.applyChanges(ruleType, operator, value);
        return rule;
    }

    public void applyChanges(PromotionRuleType ruleType, RuleOperator operator, String value) {
        this.ruleType = ruleType;
        this.operator = operator;
        this.value = value;
    }

    @PrePersist
    void initializeCreatedAt() { createdAt = Instant.now(); }

    public Long getPromotionRuleId() { return promotionRuleId; }
    public Long getPromotionId() { return promotionId; }
    public PromotionRuleType getRuleType() { return ruleType; }
    public RuleOperator getOperator() { return operator; }
    public String getValue() { return value; }
    public Instant getCreatedAt() { return createdAt; }
}
