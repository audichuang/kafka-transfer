package com.catchaybk.streams.transform;

import com.catchaybk.streams.model.Transaction;
import com.catchaybk.streams.model.TransactionTimeline;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.kstream.ValueTransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.ArrayList;

@Slf4j
public class TransactionTimelineTransformer
        implements ValueTransformerSupplier<Transaction, TransactionTimeline> {

    @Override
    public ValueTransformer<Transaction, TransactionTimeline> get() {
        return new ValueTransformer<Transaction, TransactionTimeline>() {
            private ProcessorContext context;
            private KeyValueStore<String, TransactionTimeline> timelineStore;

            @SuppressWarnings("unchecked")
            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.timelineStore = (KeyValueStore<String, TransactionTimeline>) context
                        .getStateStore("transaction-timeline-store");
            }

            @Override
            public TransactionTimeline transform(Transaction transaction) {
                if (transaction == null) {
                    return null;
                }

                String customerId = transaction.getCustomerId();
                TransactionTimeline timeline = timelineStore.get(customerId);

                if (timeline == null) {
                    timeline = new TransactionTimeline();
                    timeline.setCustomerId(customerId);
                    timeline.setTransactions(new ArrayList<>());
                }

                Transaction newTransaction = Transaction.builder()
                        .transactionId(transaction.getTransactionId())
                        .customerId(transaction.getCustomerId())
                        .amount(transaction.getAmount())
                        .type(transaction.getType())
                        .timestamp(transaction.getTimestamp())
                        .status(transaction.getStatus())
                        .build();

                timeline.getTransactions().add(newTransaction);
                timelineStore.put(customerId, timeline);

                log.info("已更新客戶時間軸 - 客戶ID: {}, 交易編號: {}",
                        customerId, transaction.getTransactionId());

                return timeline;
            }

            @Override
            public void close() {
            }
        };
    }
}
