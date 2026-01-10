package com.kmbank.repository;

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

    // ============ BASIC QUERIES ============

    // Find by reference number
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    // Get transactions by account (source or destination)
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.sourceAccount sa " +
            "LEFT JOIN t.destinationAccount da " +
            "WHERE sa.id = :accountId OR da.id = :accountId " +
            "ORDER BY t.createdAt DESC")
    Page<Transaction> findByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    // Find by transaction type
    Page<Transaction> findByTransactionType(Transaction.TransactionType type, Pageable pageable);

    // ============ USER-BASED QUERIES ============

    // Get recent transactions for a user
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.sourceAccount sa " +
            "LEFT JOIN t.destinationAccount da " +
            "WHERE sa.user.id = :userId OR da.user.id = :userId " +
            "ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    // Count transactions for user
    @Query("SELECT COUNT(t) FROM Transaction t " +
            "LEFT JOIN t.sourceAccount sa " +
            "LEFT JOIN t.destinationAccount da " +
            "WHERE sa.user.id = :userId OR da.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Count by created at after
    long countByCreatedAtAfter(LocalDateTime dateTime);

    // ============ SUM/TOTAL QUERIES ============

    // Get total by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type")
    BigDecimal getTotalByType(@Param("type") Transaction.TransactionType type);

    // Get total by type and date after
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.transactionType = :type AND t.createdAt >= :dateAfter")
    BigDecimal getTotalByTypeAndDateAfter(@Param("type") Transaction.TransactionType type, @Param("dateAfter") LocalDateTime dateAfter);

    // Sum deposits for user in period (method name: sumDepositsByUserIdSince)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "JOIN t.destinationAccount da " +
            "WHERE da.user.id = :userId AND t.transactionType = 'DEPOSIT' AND t.createdAt >= :since")
    BigDecimal sumDepositsByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Sum withdrawals for user in period (method name: sumWithdrawalsByUserIdSince)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "JOIN t.sourceAccount sa " +
            "WHERE sa.user.id = :userId AND t.transactionType = 'WITHDRAWAL' AND t.createdAt >= :since")
    BigDecimal sumWithdrawalsByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // ALTERNATIVE NAMES - Get total deposits by user since date
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "JOIN t.destinationAccount da " +
            "WHERE da.user.id = :userId AND t.transactionType = 'DEPOSIT' AND t.createdAt >= :since")
    BigDecimal getTotalDepositsByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // ALTERNATIVE NAMES - Get total withdrawals by user since date
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "JOIN t.sourceAccount sa " +
            "WHERE sa.user.id = :userId AND t.transactionType = 'WITHDRAWAL' AND t.createdAt >= :since")
    BigDecimal getTotalWithdrawalsByUserIdSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // ============ ANALYTICS QUERIES ============

    // Get daily stats - returns list of Object arrays [date, deposits, withdrawals, count]
    @Query("SELECT CAST(t.createdAt AS date) as date, " +
            "SUM(CASE WHEN t.transactionType = 'DEPOSIT' THEN t.amount ELSE 0 END) as deposits, " +
            "SUM(CASE WHEN t.transactionType = 'WITHDRAWAL' THEN t.amount ELSE 0 END) as withdrawals, " +
            "COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.createdAt >= :startDate " +
            "GROUP BY CAST(t.createdAt AS date) " +
            "ORDER BY date")
    List<Object[]> getDailyStats(@Param("startDate") LocalDateTime startDate);
}