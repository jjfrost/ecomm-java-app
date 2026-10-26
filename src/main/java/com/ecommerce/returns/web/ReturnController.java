package com.ecommerce.returns.web;

import com.ecommerce.common.api.PageResponse;
import com.ecommerce.returns.dto.ReturnDtos.AuthorizeReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.CreateReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.ReceiveReturnRequest;
import com.ecommerce.returns.dto.ReturnDtos.ReturnDetailResponse;
import com.ecommerce.returns.dto.ReturnDtos.ReturnRequestResponse;
import com.ecommerce.returns.dto.ReturnDtos.SetDispositionRequest;
import com.ecommerce.returns.model.ReturnStatus;
import com.ecommerce.returns.service.ReturnService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/returns")
public class ReturnController {
    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @GetMapping("/{returnRequestId}")
    @PreAuthorize("isAuthenticated()")
    public ReturnDetailResponse getReturn(@PathVariable @Positive Long returnRequestId) {
        return returnService.getReturn(returnRequestId);
    }

    @GetMapping("/authorization/{authorizationCode}")
    @PreAuthorize("isAuthenticated()")
    public ReturnDetailResponse getByAuthorizationCode(
            @PathVariable @NotBlank @Size(max = 100) String authorizationCode) {
        return returnService.getByAuthorizationCode(authorizationCode);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public PageResponse<ReturnRequestResponse> findReturns(
            @RequestParam(required = false) @Positive Long orderId,
            @RequestParam(required = false) @Positive Long customerId,
            @RequestParam(required = false) ReturnStatus status,
            @PageableDefault(size = 25, sort = "requestedAt") Pageable pageable) {
        return returnService.findReturns(orderId, customerId, status, pageable);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReturnDetailResponse> createReturn(
            @Valid @RequestBody CreateReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(returnService.createReturn(request));
    }

    @PatchMapping("/{returnRequestId}/authorization")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ReturnRequestResponse authorize(
            @PathVariable @Positive Long returnRequestId,
            @Valid @RequestBody AuthorizeReturnRequest request) {
        return returnService.authorize(returnRequestId, request);
    }

    @PostMapping("/{returnRequestId}/receipt")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ReturnRequestResponse receive(
            @PathVariable @Positive Long returnRequestId,
            @Valid @RequestBody ReceiveReturnRequest request) {
        return returnService.receive(returnRequestId, request);
    }

    @PatchMapping("/{returnRequestId}/items/{returnItemId}/disposition")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ReturnDetailResponse setDisposition(
            @PathVariable @Positive Long returnRequestId,
            @PathVariable @Positive Long returnItemId,
            @Valid @RequestBody SetDispositionRequest request) {
        return returnService.setDisposition(returnRequestId, returnItemId, request);
    }

    @PostMapping("/{returnRequestId}/complete")
    @PreAuthorize("hasAnyRole('OPERATIONS', 'ADMIN')")
    public ReturnRequestResponse complete(@PathVariable @Positive Long returnRequestId) {
        return returnService.complete(returnRequestId);
    }

    @PostMapping("/{returnRequestId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ReturnRequestResponse cancel(
            @PathVariable @Positive Long returnRequestId,
            @RequestParam @NotBlank @Size(max = 500) String reason) {
        return returnService.cancel(returnRequestId, reason);
    }
}
