package com.kmbank.controller;

import com.kmbank.dto.DTOs.*;
import com.kmbank.security.CustomUserDetails;
import com.kmbank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionsByAccount(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Page<TransactionResponse> transactions = transactionService
                .getTransactionsByAccountId(accountId, userDetails.getUser().getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<TransactionResponse> transactions = transactionService
                .getRecentTransactionsByUserId(userDetails.getUser().getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @PathVariable String referenceNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TransactionResponse transaction = transactionService
                .getTransactionByReference(referenceNumber, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositWithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TransactionResponse transaction = transactionService.deposit(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit successful", transaction));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TransactionResponse transaction = transactionService.withdraw(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal successful", transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        TransactionResponse transaction = transactionService.transfer(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer successful", transaction));
    }
}
