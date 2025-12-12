package com.kmbank.dto;

import com.kmbank.entity.Account;
import com.kmbank.entity.Transaction;
import com.kmbank.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DTOs {

    // ============ AUTH DTOs ============
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank(message = "Email is Required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String password;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String phoneNumber;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank(message = "Email is Required")
        @Email(message = "Invalid  email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String type;
        private UserResponse user;
    }

    // ======== USER DTOs ==========

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String phoneNumber;
        private String profileImage;
        private String role;
        private Boolean enabled;
        private LocalDateTime createdAt;

        public static UserResponse fromEntity(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .phoneNumber(user.getPhoneNumber())
                    .profileImage(user.getProfileImage())
                    .role(user.getRole().name())
                    .enabled(user.getEnabled())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateUserRequest {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String profileImage;
    }

    // ======= ACCOUNT DTOs ======

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAccountRequest {
        @NotBlank(message = "Account name is required")
        private String accountName;

        @NotNull(message = "Account type is required")
        private Account.AccountType accountType;

        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountResponse {
        private Long id;
        private String accountNumber;
        private String accountName;
        private String accountType;
        private BigDecimal balance;
        private String currency;
        private String status;
        private LocalDateTime createdAt;

        public static AccountResponse fromEntity(Account account) {
            return AccountResponse.builder()
                    .id(account.getId())
                    .accountNumber(account.getAccountNumber())
                    .accountName(account.getAccountName())
                    .accountType(account.getAccountType().name())
                    .balance(account.getBalance())
                    .currency(account.getCurrency())
                    .status(account.getStatus().name())
                    .createdAt(account.getCreatedAt())
                    .build();
        }
    }

    // ========== TRANSACTION DTOs ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferRequest {
        @NotNull(message = "Source account ID is required")
        private Long sourceAccountId;

        @NotBlank(message = "Destination account number is required")
        private String destinationAccountNumber;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
        private String recipientName;
        private String recipientBank;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepositWithdrawRequest {
        @NotNull(message = "Account ID is required")
        private Long accountId;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionResponse {
        private Long id;
        private String referenceNumber;
        private String transactionType;
        private BigDecimal amount;
        private String currency;
        private String description;
        private String status;
        private String sourceAccountNumber;
        private String destinationAccountNumber;
        private String recipientName;
        private String recipientBank;
        private BigDecimal balanceAfter;
        private LocalDateTime createdAt;
        private boolean isCredit;

        public static TransactionResponse fromEntity(Transaction transaction, Long viewingAccountId) {
            boolean isCredit = transaction.getDestinationAccount() != null &&
                              transaction.getDestinationAccount().getId().equals(viewingAccountId);

            return TransactionResponse.builder()
                    .id(transaction.getId())
                    .referenceNumber(transaction.getReferenceNumber())
                    .transactionType(transaction.getTransactionType().name())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .description(transaction.getDescription())
                    .status(transaction.getStatus().name())
                    .sourceAccountNumber(transaction.getSourceAccount() != null ?
                            transaction.getSourceAccount().getAccountNumber() : null)
                    .destinationAccountNumber(transaction.getDestinationAccount() != null ?
                            transaction.getDestinationAccount().getAccountNumber() : transaction.getRecipientAccount())
                    .recipientName(transaction.getRecipientName())
                    .recipientBank(transaction.getRecipientBank())
                    .balanceAfter(transaction.getBalanceAfter())
                    .createdAt(transaction.getCreatedAt())
                    .isCredit(isCredit)
                    .build();
        }
        public static TransactionResponse fromEntity(Transaction transaction) {
            return fromEntity(transaction, null);
        }
    }

    // ============ DASHBOARD DTOs ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardResponse {
        private BigDecimal totalBalance;
        private long totalAccounts;
        private long totalTransactions;
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpenses;
        private List<AccountResponse> accounts;
        private List<TransactionResponse> recentTransactions;
    }

    // ============ API RESPONSE ============

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .build();
        }
    }


}
