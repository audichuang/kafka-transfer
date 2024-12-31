package com.catchaybk.customer.service;

import com.catchaybk.streams.model.Transaction;
import com.catchaybk.streams.model.TransactionTimeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import com.catchaybk.customer.service.AccountService;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConsumer {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final AccountService accountService;

    @KafkaListener(topics = "transaction-timeline", containerFactory = "timelineKafkaListenerContainerFactory")
    public void consumeTimeline(TransactionTimeline timeline) {
        String lastUpdatedStr = timeline.getLastUpdated() != null ? timeline.getLastUpdated().format(formatter) : "N/A";

        log.info("收到交易時間軸資料 - 客戶ID: {}, 交易筆數: {}, 最後更新時間: {}",
                timeline.getCustomerId(),
                timeline.getTransactions().size(),
                lastUpdatedStr);
    }

    @KafkaListener(topics = "transactions", containerFactory = "transactionKafkaListenerContainerFactory")
    public void consumeTransaction(Transaction transaction) {
        try {
            boolean isDeposit = transaction.getType() == Transaction.TransactionType.DEPOSIT;
            accountService.processTransaction(
                    transaction.getCustomerId(),
                    transaction.getAmount(),
                    isDeposit);
            log.info("交易處理成功 - 客戶ID: {}, 類型: {}, 金額: {}",
                    transaction.getCustomerId(),
                    transaction.getType(),
                    transaction.getAmount());
        } catch (Exception e) {
            log.error("交易處理失敗 - 客戶ID: {}, 原因: {}",
                    transaction.getCustomerId(),
                    e.getMessage());
        }
    }

    @KafkaListener(topics = "large-transactions")
    public void consumeLargeTransactions(Transaction transaction) {
        log.info("收到大額交易通知 - 交易編號: {}, 客戶ID: {}, 交易金額: {}",
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getAmount());
    }

    @KafkaListener(topics = "suspicious-transactions")
    public void consumeSuspiciousTransactions(String alert) {
        log.info("收到可疑交易警報: {}", alert);
    }

    @KafkaListener(topics = "risk-evaluated-transactions")
    public void consumeRiskEvaluatedTransactions(Transaction transaction) {
        log.info("收到風險評估結果 - 交易編號: {}, 客戶ID: {}, 交易類型: {}, 風險分數: {}",
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getType(),
                transaction.getRiskScore());
    }
}
