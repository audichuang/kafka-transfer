package com.catchaybk.streams.transform;

import com.catchaybk.streams.model.Transaction;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MetricsReporter {
    // 計數器名稱可以保持英文，因為這通常用於監控系統
    private static final Counter transactionCounter = Metrics.counter("transactions.processed");

    public static void recordTransaction(Transaction transaction) {
        try {
            transactionCounter.increment();
            log.info("已處理交易，交易編號: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("記錄交易指標時發生錯誤: {}", e.getMessage(), e);
            notifyAdmin("記錄指標失敗: " + e.getMessage());
        }
    }

    public static void notifyAdmin(String message) {
        log.error("串流處理錯誤: {}", message);
        // 實現通知邏輯
    }
}
