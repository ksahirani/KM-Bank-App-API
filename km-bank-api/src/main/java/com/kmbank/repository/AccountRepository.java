package com.kmbank.repository;

import com.kmbank.dto.AccountTypeStatProjection;
import com.kmbank.entity.Account;
import com.kmbank.entity.User;
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
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(User user);
    List<Account> findByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.user.id = :userId AND a.status = 'ACTIVE'")
    long countActiveAccountsByUserId(@Param("userId") Long userId);

    // Admin queries
    Page<Account> findByStatus(Account.AccountStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.status = 'ACTIVE'")
    BigDecimal getSystemTotalBalance();

    long countByCreatedAtAfter(LocalDateTime dateTime);

    // ✅ Fixed: Using interface projection with native query
    @Query(value = "SELECT " +
            "CAST(account_type AS VARCHAR) AS accountType, " +
            "COUNT(*) AS count, " +
            "COALESCE(SUM(balance), 0) AS totalBalance " +
            "FROM accounts GROUP BY account_type",
            nativeQuery = true)
    List<AccountTypeStatProjection> getAccountTypeStats();
}