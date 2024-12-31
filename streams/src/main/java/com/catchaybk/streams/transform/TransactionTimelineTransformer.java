package com.catchaybk.streams.transform;

import com.catchaybk.streams.model.Transaction;
import com.catchaybk.streams.model.TransactionTimeline;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.kstream.ValueTransformerWithKeySupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class TransactionTimelineTransformer
        implements ValueTransformerWithKeySupplier<String, Transaction, TransactionTimeline> {

    @Override
    public ValueTransformerWithKey<String, Transaction, TransactionTimeline> get() {
        return new ValueTransformerWithKey<String, Transaction, TransactionTimeline>() {
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
            public TransactionTimeline transform(String key, Transaction transaction) {
                String customerId = transaction.getCustomerId();
                TransactionTimeline timeline = timelineStore.get(customerId);

                if (timeline == null) {
                    timeline = new TransactionTimeline(customerId);
                }

                timeline.addTransaction(transaction);
                timelineStore.put(customerId, timeline);

                return timeline;
            }

            @Override
            public void close() {
            }
        };
    }
}
