package com.kmbank.controller;

import com.kmbank.dto.DTOs.*;
import com.kmbank.security.CustomUserDetails;
import com.kmbank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // THIS IS THE MISSING/BROKEN ENDPOINT
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<TransactionResponse> transactions = transactionService.getTransactionsByAccountId(
                accountId, userDetails.getUser().getId(), page, size);

        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositWithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TransactionResponse transaction = transactionService.deposit(request, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", transaction));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TransactionResponse transaction = transactionService.withdraw(request, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        TransactionResponse transaction = transactionService.transfer(request, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", transaction));
    }
}