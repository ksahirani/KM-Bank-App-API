package com.kmbank.service;

import com.kmbank.dto.DTOs.*;
import com.kmbank.entity.Account;
import com.kmbank.entity.Transaction;
import com.kmbank.exception.Exceptions;
import com.kmbank.repository.AccountRepository;
import com.kmbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Page<TransactionResponse> getTransactionsByAccountId(long accountId, Long userId, int page, int size) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(tx -> TransactionResponse.fromEntity(tx, accountId));
    }

    public List<TransactionResponse> getRecentTransactionsByUserId(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findRecentByUserId(userId, pageable).stream()
                .map(TransactionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TransactionResponse getTransactionByReference(String referenceNumber, Long userId) {
        Transaction transaction = transactionRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Transaction not found"));

        boolean hasAccess = (transaction.getSourceAccount() != null &&
                            transaction.getSourceAccount().getUser().getId().equals(userId)) ||
                            (transaction.getDestinationAccount() != null &&
                             transaction.getDestinationAccount().getUser().getId().equals(userId));

        if (!hasAccess) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this transaction");
        }

        return TransactionResponse.fromEntity(transaction);
    }

    @Transactional
    public TransactionResponse deposit(DepositWithdrawRequest request, Long userId) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new Exceptions.BadRequestException("Account is not active");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(Transaction.TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Cash Deposit")
                .destinationAccount(account)
                .balanceAfter(account.getBalance())
                .build();

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.fromEntity(transaction, account.getId());
    }

    @Transactional
    public TransactionResponse withdraw(DepositWithdrawRequest request, Long userId) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new Exceptions.InsufficientFundsException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .description(request.getDescription() != null ? request.getDescription() : "Cash Withdrawal")
                .sourceAccount(account)
                .balanceAfter(account.getBalance())
                .build();

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.fromEntity(transaction, account.getId());
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, Long userId) {
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Source account not found"));

        if (!sourceAccount.getUser().getId().equals(userId)) {
            throw new Exceptions.UnauthorizedAccessException("You don't have access to this account");
        }

        if (sourceAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new Exceptions.BadRequestException("Source account is not active");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new Exceptions.InsufficientFundsException("Insufficient funds");
        }

        // Check if destination is internal or external
        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
                .orElse(null);

        // Deduct from source
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(sourceAccount);

        Transaction transaction = Transaction.builder()
                .transactionType(Transaction.TransactionType.TRANSFER)
                .amount(request.getAmount())
                .description(request.getDescription())
                .sourceAccount(sourceAccount)
                .balanceAfter(sourceAccount.getBalance())
                .build();

        if (destinationAccount != null) {
           // Internal transfer
            if (destinationAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            // Rollback source deduction
                sourceAccount.setBalance(sourceAccount.getBalance().add(request.getAmount()));
                accountRepository.save(sourceAccount);
                throw new Exceptions.BadRequestException("Destination account is not active");
            }

            destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
            accountRepository.save(destinationAccount);

            transaction.setDestinationAccount(destinationAccount);
            transaction.setRecipientName(destinationAccount.getUser().getFullName());

        } else {
            // External Transfer
            transaction.setRecipientAccount(request.getDestinationAccountNumber());
            transaction.setRecipientName(request.getRecipientName());
            transaction.setRecipientBank(request.getRecipientBank());
        }

        transaction = transactionRepository.save(transaction);

        return TransactionResponse.fromEntity(transaction, sourceAccount.getId());
    }
}