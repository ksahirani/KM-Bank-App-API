package com.kmbank.controller;

import com.kmbank.dto.DTOs.*;
import com.kmbank.security.CustomUserDetails;
import com.kmbank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AccountResponse account = accountService.getAccountById(id, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @PathVariable String accountNumber) {
        AccountResponse account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AccountResponse account = accountService.createAccount(request, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", account));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountName(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String newName = request.get("accountName");
        AccountResponse account = accountService.updateAccountName(id, newName, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Account updated successfully", account));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> closeAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        accountService.closeAccount(id, userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", null));
    }
}
