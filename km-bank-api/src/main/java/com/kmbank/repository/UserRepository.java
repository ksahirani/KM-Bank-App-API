package com.kmbank.repository;

import com.kmbank.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    //Admin queries
    List<User> findTop10ByOrderByCreatedAtDesc();

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))" +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%' , :search, '%'))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
}
