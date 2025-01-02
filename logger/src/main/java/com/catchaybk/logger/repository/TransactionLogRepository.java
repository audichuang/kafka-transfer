package com.catchaybk.logger.repository;

import com.catchaybk.logger.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    List<TransactionLog> findByCustomerId(String customerId);

    List<TransactionLog> findByTransactionTimeBetween(LocalDateTime start, LocalDateTime end);

    List<TransactionLog> findByStatus(String status);
}