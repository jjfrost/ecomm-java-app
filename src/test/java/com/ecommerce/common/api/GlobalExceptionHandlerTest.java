package com.ecommerce.common.api;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ConcurrencyConflictException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.oracle.OracleRoutineException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/orders/44");
        MDC.put("correlationId", "test-correlation");
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void mapsMissingResourceToNotFound() {
        ResponseEntity<ApiError> response = handler.handleNotFound(
                new ResourceNotFoundException("Order", 44L),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Order").contains("44");
        assertThat(response.getBody().path()).isEqualTo("/api/orders/44");
        assertThat(response.getBody().correlationId()).isEqualTo("test-correlation");
    }

    @Test
    void mapsBusinessRuleToUnprocessableEntity() {
        ResponseEntity<ApiError> response = handler.handleBusinessRule(
                new BusinessRuleException("ORDER_STATE", "Order cannot be cancelled"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().message()).isEqualTo("ORDER_STATE: Order cannot be cancelled");
    }

    @Test
    void mapsConcurrencyConflictToConflict() {
        ResponseEntity<ApiError> response = handler.handleConflict(
                new ConcurrencyConflictException("Version changed"),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("Version changed");
    }

    @Test
    void hidesDatabaseFailureDetails() {
        ResponseEntity<ApiError> response = handler.handleOracleRoutine(
                new OracleRoutineException("ecomm.ORDER_PKG.SUBMIT_ORDER", new SQLException("secret detail")),
                request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).contains("ecomm.ORDER_PKG.SUBMIT_ORDER");
        assertThat(response.getBody().message()).doesNotContain("secret detail");
    }

    @Test
    void usesFallbackWhenCorrelationContextIsMissing() {
        MDC.clear();

        ResponseEntity<ApiError> response = handler.handleNotFound(
                new ResourceNotFoundException("Order", 44L),
                request);

        assertThat(response.getBody().correlationId()).isEqualTo("unavailable");
    }
}
