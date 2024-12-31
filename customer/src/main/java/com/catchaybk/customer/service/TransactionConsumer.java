package com.catchaybk.customer.service;

import com.catchaybk.streams.model.Transaction;
import com.catchaybk.streams.model.TransactionTimeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionConsumer {

    @KafkaListener(topics = "transaction-timeline")
    public void consumeTimeline(TransactionTimeline timeline) {
        log.info("Consumer received timeline - Customer: {}, Transactions count: {}, Last updated: {}",
                timeline.getCustomerId(),
                timeline.getTransactions().size(),
                timeline.getLastUpdated());
    }

    @KafkaListener(topics = "large-transactions")
    public void consumeLargeTransactions(Transaction transaction) {
        log.info("Consumer received large transaction - ID: {}, Customer: {}, Amount: {}",
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getAmount());
    }

    @KafkaListener(topics = "suspicious-transactions")
    public void consumeSuspiciousTransactions(String alert) {
        log.info("Consumer received suspicious activity alert: {}", alert);
    }
}