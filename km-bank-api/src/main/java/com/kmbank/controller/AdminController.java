package com.kmbank.controller;

import com.kmbank.dto.DTOs.*;
import com.kmbank.entity.Account;
import com.kmbank.entity.User;
import com.kmbank.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // ============ DASHBOARD =============

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse dashboard = adminService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // ============ USER MANAGEMENT ===========

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<UserResponse> users = adminService.getAllUsers(page, size, search);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        UserDetailResponse user = adminService.getUserDetail(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        UserResponse user = adminService.toggleUserStatus(id);
        String status = user.getEnabled() ? "enabled" : "disabled";
        return ResponseEntity.ok(ApiResponse.success("User" + status + " successfully", user));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long id,
            @PathVariable Map<String, String> request) {
        User.Role newRole = User.Role.valueOf(request.get("role").toUpperCase());
        UserResponse user = adminService.updateUserRole(id, newRole);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // ========= ACCOUNT MANAGEMENT ==========

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Page<AccountResponse> accounts = adminService.getAllAccounts(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<AdminAccountDetailResponse>> getAccountDetail(@PathVariable Long id) {
        AdminAccountDetailResponse account = adminService.getAccountDetail(id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    @PatchMapping("/accounts/{id}/status")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        Account.AccountStatus newStatus = Account.AccountStatus.valueOf(request.get("status").toUpperCase());
        AccountResponse account = adminService.updateAccountStatus(id, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Account status updated successfully", account));
    }

    @PostMapping("/accounts/{id}/adjust")
    public ResponseEntity<ApiResponse<AccountResponse>> adjustAccountBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        BigDecimal adjustment = new BigDecimal(request.get("amount").toString());
        String reason = (String) request.getOrDefault("reason", "Admin adjustment");
        AccountResponse account = adminService.adjustAccountBalance(id, adjustment, reason);
        return ResponseEntity.ok(ApiResponse.success("Account balance adjusted successfully", account));
    }

    // ============= TRANSACTION MANAGEMENT ==============

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<AdminTransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type) {
        Page<AdminTransactionResponse> transactions = adminService.getAllTransactions(page, size, type);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<AdminTransactionResponse>> getTransactionDetail(@PathVariable Long id) {
        AdminTransactionResponse transaction = adminService.getTransactionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    // ============= ANALYTICS =================

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics(
            @RequestParam(defaultValue = "month") String period) {
        AnalyticsResponse analytics = adminService.getAnalytics(period);
        return ResponseEntity.ok(ApiResponse.success(analytics));
    }
}
