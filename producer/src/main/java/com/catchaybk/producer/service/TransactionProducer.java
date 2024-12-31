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
        log.info("【交易產生】準備發送 \n" +
                        "├─ 交易編號: {} \n" +
                        "└─ 客戶ID: {}",
                transaction.getTransactionId(),
                transaction.getCustomerId());

        kafkaTemplate.send("transactions",
                        transaction.getCustomerId(),
                        transaction)
                .thenAccept(result -> log.info("【交易發送】成功 - 交易編號: {}",
                        transaction.getTransactionId()))
                .exceptionally(ex -> {
                    log.error("【交易發送】失敗 \n" +
                                    "├─ 交易編號: {} \n" +
                                    "└─ 錯誤訊息: {}",
                            transaction.getTransactionId(),
                            ex.getMessage());
                    return null;
                });
    }
}
