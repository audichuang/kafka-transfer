package com.catchaybk.logger.service;

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

    @KafkaListener(topics = "transaction-logs", groupId = "logger-consumer", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTransactionLog(Transaction transaction) {
        log.info("收到交易日誌 - 交易ID: {}, 狀態: {}",
                transaction.getTransactionId(),
                transaction.getStatus());

        loggingService.logTransaction(transaction);
    }
}