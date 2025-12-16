package com.kmbank.service;

import com.kmbank.dto.DTOs.*;
import com.kmbank.entity.Account;
import com.kmbank.entity.Transaction;
import com.kmbank.entity.User;
import com.kmbank.exception.Exceptions;
import com.kmbank.repository.AccountRepository;
import com.kmbank.repository.TransactionRepository;
import com.kmbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // ============== DASHBOARD STATS ==============

    public AdminDashboardResponse getAdminDashboard() {
        long totalUsers = userRepository.count();
        long totalAccounts = accountRepository.count();
        long totalTransactions = transactionRepository.count();

        BigDecimal totalDeposits = transactionRepository.getTotalByType(Transaction.TransactionType.DEPOSIT);
        BigDecimal totalWithdrawals = transactionRepository.getTotalByType(Transaction.TransactionType.WITHDRAWAL);
        BigDecimal totalTransfers = transactionRepository.getTotalByType(Transaction.TransactionType.TRANSFER);
        BigDecimal systemBalance = accountRepository.getSystemTotalBalance();

    // Get recent activity
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long newUsersToday = userRepository.countByCreatedAtAfter(last24Hours);
        long transactionsToday = transactionRepository.countByCreatedAtAfter(last24Hours);

    // Get recent users and transactions
        List<UserResponse> recentUsers = userRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        List<AdminTransactionResponse> recentTransactions = transactionRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(AdminTransactionResponse::fromEntity)
                .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalAccounts(totalAccounts)
                .totalTransactions(totalTransactions)
                .totalDeposits(totalDeposits != null ? totalDeposits : BigDecimal.ZERO)
                .totalWithdrawals(totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO)
                .totalTransfers(totalTransfers != null ? totalTransfers : BigDecimal.ZERO)
                .systemBalance(systemBalance != null ? systemBalance : BigDecimal.ZERO)
                .newUsersToday(newUsersToday)
                .transactionsToday(transactionsToday)
                .recentUsers(recentUsers)
                .recentTransactions(recentTransactions)
                .build();
    }

    // ================ USER MANAGEMENT =================

    public Page<UserResponse> getAllUsers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.searchUsers(search.trim(), pageable);

        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(UserResponse::fromEntity);
    }

    public UserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        List<AccountResponse> accounts = accountRepository.findByUserId(userId)
                .stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());

        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(userId);
        long transactionCount = transactionRepository.countByUserId(userId);

        return UserDetailResponse.builder()
                .user(UserResponse.fromEntity(user))
                .accounts(accounts)
                .totalBalance(totalBalance != null ? totalBalance : BigDecimal.ZERO)
                .transactionCount(transactionCount)
                .build();
    }

    @Transactional
    public UserResponse toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        user.setEnabled(!user.getEnabled());
        user = userRepository.save(user);

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        user.setRole(newRole);
        user = userRepository.save(user);

        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        // Check if user has any balance
        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(userId);
        if (totalBalance != null && totalBalance.compareTo(BigDecimal.ZERO) > 0) {
            throw new Exceptions.BadRequestException("Cannot delete user with remaining balance");
        }

        userRepository.delete(user);
    }

    // =============== ACCOUNT MANAGEMENT =============

    public Page<AccountResponse> getAllAccounts(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Account> accounts;

        if (status != null && !status.equalsIgnoreCase("all")) {
            Account.AccountStatus accountStatus = Account.AccountStatus.valueOf(status.toUpperCase());
            accounts = accountRepository.findByStatus(accountStatus, pageable);
        } else {
            accounts = accountRepository.findAll(pageable);
        }

        return accounts.map(AccountResponse::fromEntity);
    }

    public AdminAccountDetailResponse getAccountDetail(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        Pageable pageable = PageRequest.of(0, 20);
        List<TransactionResponse> recentTransactions = transactionRepository
                .findByAccount(account, pageable)
                .stream()
                .map(t -> TransactionResponse.fromEntity(t, accountId))
                .collect(Collectors.toList());

        return AdminAccountDetailResponse.builder()
                .account(AccountResponse.fromEntity(account))
                .ownerName(account.getUser().getFullName())
                .ownerEmail(account.getUser().getEmail())
                .ownerId(account.getUser().getId())
                .recentTransactions(recentTransactions)
                .build();
    }

    @Transactional
    public AccountResponse updateAccountStatus(Long accountId, Account.AccountStatus newStatus) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        account.setStatus(newStatus);
        account = accountRepository.save(account);

        return AccountResponse.fromEntity(account);
    }

    @Transactional
    public AccountResponse adjustAccountBalance(Long accountId, BigDecimal adjustment, String reason) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        BigDecimal newBalance = account.getBalance().add(adjustment);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new Exceptions.BadRequestException("Adjustment would result in negative balance");
        }

        account.setBalance(newBalance);
        account = accountRepository.save(account);

        // Create adjustment transaction
        Transaction transaction = Transaction.builder()
                .transactionType(adjustment.compareTo(BigDecimal.ZERO) > 0
                         ? Transaction.TransactionType.DEPOSIT
                         : Transaction.TransactionType.WITHDRAWAL)
                .amount(adjustment.abs())
                .description("Admin adjustment" + reason)
                .destinationAccount(adjustment.compareTo(BigDecimal.ZERO) > 0 ? account : null)
                .sourceAccount(adjustment.compareTo(BigDecimal.ZERO) < 0 ? account : null)
                .balanceAfter(newBalance)
                .build();

        transactionRepository.save(transaction);
        return AccountResponse.fromEntity(account);
    }

    // ============== TRANSACTION MANAGEMENT =============

    public Page<AdminTransactionResponse> getAllTransactions(int page, int size, String type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Transaction> transactions;
        if (type != null && !type.equalsIgnoreCase("all")) {
            Transaction.TransactionType txType = Transaction.TransactionType.valueOf(type.toUpperCase());
            transactions = transactionRepository.findByTransactionType(txType, pageable);
        } else {
            transactions = transactionRepository.findAll(pageable);
        }

        return transactions.map(AdminTransactionResponse::fromEntity);
    }

    public AdminTransactionResponse getTransactionDetail(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Transaction not found"));

        return  AdminTransactionResponse.fromEntity(transaction);
    }

    // ===================== ANALYTICS =====================

    public AnalyticsResponse getAnalytics(String period) {
        LocalDateTime startDate;
        switch (period.toLowerCase()) {
            case "week":
                startDate = LocalDateTime.now().minusWeeks(1);
                break;
            case "month":
                startDate = LocalDateTime.now().minusMonths(1);
                break;
            case "year":
                startDate = LocalDateTime.now().minusYears(1);
                break;
            default:
                startDate = LocalDateTime.now().minusMonths(1);

        }

        List<DailyStatResponse> dailyStats = transactionRepository.getDailyStats(startDate);
        List<AccountTypeStatResponse> accountTypeStats = accountRepository.getAccountTypeStats();

        BigDecimal totalDeposits = transactionRepository.getTotalByTypeAndDateAfter(
                Transaction.TransactionType.DEPOSIT, startDate);
        BigDecimal totalWithdrawals = transactionRepository.getTotalByTypeAndDateAfter(
                Transaction.TransactionType.WITHDRAWAL, startDate);
        long newUsers = userRepository.countByCreatedAtAfter(startDate);
        long newAccounts = accountRepository.countByCreatedAtAfter(startDate);

        return AnalyticsResponse.builder()
                .dailyStats(dailyStats)
                .accountTypeStats(accountTypeStats)
                .totalDeposits(totalDeposits != null ? totalDeposits : BigDecimal.ZERO)
                .totalWithdrawals(totalWithdrawals != null ? totalWithdrawals : BigDecimal.ZERO)
                .newUsers(newUsers)
                .newAccounts(newAccounts)
                .period(period)
                .build();
    }
}
