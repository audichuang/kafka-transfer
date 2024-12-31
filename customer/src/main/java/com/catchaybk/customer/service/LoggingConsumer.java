package com.catchaybk.customer.service;

import com.catchaybk.streams.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggingConsumer {
    private final LoggingService loggingService;

    @KafkaListener(topics = "transaction-logs", groupId = "logging-consumer", containerFactory = "transactionKafkaListenerContainerFactory")
    public void consumeTransactionForLogging(Transaction transaction) {
        log.info("收到交易日誌請求 - 交易ID: {}", transaction.getTransactionId());
        loggingService.logTransaction(transaction, "RECEIVED", null);
    }
}