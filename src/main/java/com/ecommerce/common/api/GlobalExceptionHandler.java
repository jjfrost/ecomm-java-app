package com.ecommerce.common.api;

import com.ecommerce.common.exception.BusinessRuleException;
import com.ecommerce.common.exception.ConcurrencyConflictException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.common.oracle.OracleRoutineException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request) {
        String message = exception.getRuleCode() + ": " + exception.getMessage();
        return response(HttpStatus.UNPROCESSABLE_ENTITY, message, request);
    }

    @ExceptionHandler({
            ConcurrencyConflictException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<FieldViolation> violations = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toViolation)
                .toList();
        ApiError body = ApiError.withViolations(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                correlationId(),
                violations);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        List<FieldViolation> violations = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldViolation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .toList();
        ApiError body = ApiError.withViolations(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request validation failed",
                request.getRequestURI(),
                correlationId(),
                violations);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "Request body is malformed or has invalid values", request);
    }

    @ExceptionHandler(OracleRoutineException.class)
    public ResponseEntity<ApiError> handleOracleRoutine(
            OracleRoutineException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Database operation failed for " + exception.getRoutineName(),
                request);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String message,
            HttpServletRequest request) {
        ApiError body = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                correlationId());
        return ResponseEntity.status(status).body(body);
    }

    private FieldViolation toViolation(FieldError error) {
        return new FieldViolation(error.getField(), error.getDefaultMessage(), error.getRejectedValue());
    }

    private String correlationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId == null ? "unavailable" : correlationId;
    }
}
