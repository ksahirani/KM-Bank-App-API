package com.kmbank.repository;

import com.kmbank.dto.DTOs.DailyStatResponse;
import com.kmbank.entity.Account;
import com.kmbank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount = :account OR t.destinationAccount = :account ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccount(@Param("account") Account account, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId) ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId) ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sourceAccount.user.id = :userId AND t.transactionType = 'WITHDRAWAL' AND t.createdAt >= :startDate")
    BigDecimal getTotalWithdrawalsByUserIdSince(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.destinationAccount.user.id = :userId AND t.transactionType = 'DEPOSIT' AND t.createdAt >= :startDate")
    BigDecimal getTotalDepositsByUserIdSince(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE (t.sourceAccount.user.id = :userId OR t.destinationAccount.user.id = :userId)")
    long countByUserId(@Param("userId") Long userId);

    // Admin queries
    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    Page<Transaction> findByTransactionType(Transaction.TransactionType type, Pageable pageable);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type")
    BigDecimal getTotalByType(@Param("type") Transaction.TransactionType type);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type AND t.createdAt >= :startDate")
    BigDecimal getTotalByTypeAndDateAfter(@Param("type") Transaction.TransactionType type, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT new com.kmbank.dto.DTOs$DailyStatResponse(" +
            "CAST(CAST(t.createdAt AS date) AS string), " +
            "COALESCE(SUM(CASE WHEN t.transactionType = 'DEPOSIT' THEN t.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.transactionType = 'WITHDRAWAL' THEN t.amount ELSE 0 END), 0), " +
            "COUNT(t)) " +
            "FROM Transaction t WHERE t.createdAt >= :startDate " +
            "GROUP BY CAST(t.createdAt AS date) ORDER BY CAST(t.createdAt AS date)")
    List<DailyStatResponse> getDailyStats(@Param("startDate") LocalDateTime startDate);
}
