package com.kmbank.service;

import com.kmbank.dto.DTOs.*;
import com.kmbank.repository.AccountRepository;
import com.kmbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public DashboardResponse getDashboard(Long userId) {
        BigDecimal totalBalance = accountRepository.getTotalBalanceByUserId(userId);
        long totalAccounts = accountRepository.countActiveAccountsByUserId(userId);
        long totalTransactions = transactionRepository.countByUserId(userId);

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        BigDecimal monthlyIncome = transactionRepository.getTotalDepositsByUserIdSince(userId, startOfMonth);
        BigDecimal monthlyExpenses = transactionRepository.getTotalWithdrawalsByUserIdSince(userId, startOfMonth);

        List<AccountResponse> accounts = accountRepository.findByUserId(userId).stream()
                .filter(a -> a.getStatus() == com.kmbank.entity.Account.AccountStatus.ACTIVE)
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());

        List<TransactionResponse> recentTransactions = transactionRepository
                .findRecentByUserId(userId, PageRequest.of(0, 10)).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalBalance(totalBalance != null ? totalBalance : BigDecimal.ZERO)
                .totalAccounts(totalAccounts)
                .totalTransactions(totalTransactions)
                .monthlyIncome(monthlyIncome != null ? monthlyIncome : BigDecimal.ZERO)
                .monthlyExpenses(monthlyExpenses != null ? monthlyExpenses : BigDecimal.ZERO)
                .accounts(accounts)
                .recentTransactions(recentTransactions)
                .build();
    }
}
