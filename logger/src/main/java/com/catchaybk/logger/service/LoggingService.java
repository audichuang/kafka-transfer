package com.catchaybk.logger.service;

import com.catchaybk.logger.entity.TransactionLog;
import com.catchaybk.logger.repository.TransactionLogRepository;
import com.catchaybk.streams.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingService {
    private final TransactionLogRepository logRepository;

    @Transactional
    public void logTransaction(Transaction transaction) {
        try {
            TransactionLog transactionLog = TransactionLog.builder()
                    .transactionId(transaction.getTransactionId())
                    .customerId(transaction.getCustomerId())
                    .transactionType(transaction.getType().toString())
                    .amount(transaction.getAmount())
                    .status(transaction.getStatus().toString())
                    .transactionTime(transaction.getTimestamp())
                    .build();

            logRepository.save(transactionLog);

            log.info("交易日誌已保存 - 交易ID: {}, 狀態: {}",
                    transaction.getTransactionId(),
                    transaction.getStatus());
        } catch (Exception e) {
            log.error("保存交易日誌失敗 - 交易ID: {}, 錯誤: {}",
                    transaction.getTransactionId(),
                    e.getMessage());
        }
    }
}