package com.kmbank.service;

import com.kmbank.dto.DTOs.*;
import com.kmbank.entity.Account;
import com.kmbank.entity.User;
import com.kmbank.exception.Exceptions;
import com.kmbank.repository.AccountRepository;
import com.kmbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public List<AccountResponse> getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(AccountResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountById(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }

        return AccountResponse.fromEntity(account);

    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));
        return AccountResponse.fromEntity(account);
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        Account account = Account.builder()
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .user(user)
                .build();

        account = accountRepository.save(account);

        return AccountResponse.fromEntity(account);
    }

    @Transactional
    public AccountResponse updateAccountName(Long accountId, String newName, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }
        account.setAccountName(newName);
        account = accountRepository.save(account);

        return  AccountResponse.fromEntity(account);
    }

    @Transactional
    public void closeAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }

        if (account.getBalance().compareTo(java.math.BigDecimal.ZERO) != 0) {
            throw new Exceptions.BadRequestException("Account balance must be zero before closing");
        }

        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);
    }
}
