package com.ecommerce.common.persistence;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditableEntityTest {
    @Test
    void initializesAndUpdatesTimestamps() {
        TestEntity entity = new TestEntity();

        entity.initialize();
        Instant createdAt = entity.getCreatedAt();
        Instant firstUpdatedAt = entity.getUpdatedAt();
        entity.touch();

        assertThat(createdAt).isNotNull();
        assertThat(firstUpdatedAt).isNotNull();
        assertThat(entity.getVersion()).isZero();
        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(firstUpdatedAt);
    }

    private static final class TestEntity extends AuditableEntity {
        void initialize() {
            initializeAuditFields();
        }

        void touch() {
            updateAuditFields();
        }
    }
}
