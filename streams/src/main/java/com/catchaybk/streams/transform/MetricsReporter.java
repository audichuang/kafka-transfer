package com.catchaybk.streams.transform;

import com.catchaybk.streams.model.Transaction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsReporter {
    private static final Counter transactionCounter = Metrics.counter("transactions.processed");

    public static void recordTransaction(Transaction transaction) {
        try {
            transactionCounter.increment();
            log.info("Processed transaction: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("Error recording transaction metrics: {}", e.getMessage(), e);
            notifyAdmin("Error recording metrics: " + e.getMessage());
        }
    }

    public static void notifyAdmin(String message) {
        log.error("Stream Error: {}", message);
        // 實現通知邏輯
    }
}
