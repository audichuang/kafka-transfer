package com.catchaybk.producer.service;

import com.catchaybk.streams.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProducer {
    private final KafkaTemplate<String, Transaction> kafkaTemplate;

    private Transaction generateTransaction() {
        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .customerId("customer-" + (int) (Math.random() * 5))
                .amount(BigDecimal.valueOf(Math.random() * 2000000))
                .type(Transaction.TransactionType.DEPOSIT)
                .timestamp(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }

    public void sendTransaction() {
        Transaction transaction = generateTransaction();
        log.info("Producing transaction: {} for customer: {}",
                transaction.getTransactionId(),
                transaction.getCustomerId());

        kafkaTemplate.send("transactions",
                transaction.getCustomerId(),
                transaction)
                .thenAccept(result -> log.info("Successfully sent transaction: {}", transaction.getTransactionId()))
                .exceptionally(ex -> {
                    log.error("Failed to send transaction: {}", ex.getMessage());
                    return null;
                });
    }
}