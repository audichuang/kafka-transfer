package com.catchaybk.streams.config;

import com.catchaybk.streams.model.*;
import com.catchaybk.streams.transform.MetricsReporter;
import com.catchaybk.streams.transform.TransactionTimelineTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
@Slf4j
public class KafkaStreamsConfig {

        @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
        public KafkaStreamsConfiguration kStreamsConfig() {
                Map<String, Object> props = new HashMap<>();

                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "advanced-transaction-processor");
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                                LogAndContinueExceptionHandler.class);
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
                props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
                props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
                props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
                props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
                props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Transaction.class.getName());
                props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
                props.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, false);

                return new KafkaStreamsConfiguration(props);
        }

        @Bean
        public KStream<String, Transaction> kStream(StreamsBuilder streamsBuilder) {
                // 為每個 store 配置特定的 serde
                JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
                JsonSerde<TransactionStats> statsSerde = new JsonSerde<>(TransactionStats.class);
                JsonSerde<CustomerBalance> balanceSerde = new JsonSerde<>(CustomerBalance.class);
                JsonSerde<TransactionTimeline> timelineSerde = new JsonSerde<>(TransactionTimeline.class);

                // 配置每個 serde
                configureJsonSerde(transactionSerde);
                configureJsonSerde(statsSerde);
                configureJsonSerde(balanceSerde);
                configureJsonSerde(timelineSerde);

                // 配置狀態存儲
                StoreBuilder<KeyValueStore<String, TransactionTimeline>> timelineStoreBuilder = Stores
                                .keyValueStoreBuilder(
                                                Stores.persistentKeyValueStore("transaction-timeline-store"),
                                                Serdes.String(),
                                                timelineSerde);
                streamsBuilder.addStateStore(timelineStoreBuilder);

                // 1. 讀取交易流
                KStream<String, Transaction> transactionStream = streamsBuilder.stream(
                                "transactions",
                                Consumed.with(Serdes.String(), transactionSerde));

                // 添加日誌記錄
                transactionStream.peek((key, transaction) -> {
                        log.info("【串流處理】收到新交易 \n" +
                                        "├─ 鍵值: {} \n" +
                                        "├─ 交易編號: {} \n" +
                                        "├─ 客戶ID: {} \n" +
                                        "└─ 交易金額: {}",
                                        key,
                                        transaction.getTransactionId(),
                                        transaction.getCustomerId(),
                                        transaction.getAmount());

                });

                // 2. 分離大額交易和普通交易
                KStream<String, Transaction>[] branches = transactionStream.branch(
                                (key, transaction) -> transaction.getAmount().compareTo(new BigDecimal("1000000")) >= 0,
                                (key, transaction) -> true);

                KStream<String, Transaction> largeTransactions = branches[0];
                KStream<String, Transaction> normalTransactions = branches[1];

                // 3. 處理大額交易
                largeTransactions
                                .mapValues(transaction -> {
                                        transaction.setHighRisk(true);
                                        transaction.setNeedsApproval(true);
                                        return transaction;
                                })
                                .to("large-transactions");

                // 4. 計算客戶餘額
                normalTransactions
                                .groupByKey()
                                .aggregate(
                                                CustomerBalance::new,
                                                (key, transaction, balance) -> balance.updateBalance(transaction),
                                                Materialized.<String, CustomerBalance, KeyValueStore<Bytes, byte[]>>as(
                                                                "customer-balance-store")
                                                                .withKeySerde(Serdes.String())
                                                                .withValueSerde(balanceSerde));

                // 5. 檢測可疑交易
                normalTransactions
                                .groupByKey()
                                .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
                                .count()
                                .toStream()
                                .filter((key, count) -> count >= 5)
                                .mapValues(count -> new Alert("Suspicious Activity Detected"))
                                .to("suspicious-transactions");

                // 6. 每小時交易統計
                normalTransactions
                                .groupByKey()
                                .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                                .aggregate(
                                                TransactionStats::new,
                                                (key, transaction, stats) -> {
                                                        if (transaction != null && transaction.getAmount() != null) {
                                                                return stats.updateStats(transaction);
                                                        }
                                                        return stats;
                                                },
                                                Materialized.<String, TransactionStats, WindowStore<Bytes, byte[]>>as(
                                                                "hourly-stats")
                                                                .withKeySerde(Serdes.String())
                                                                .withValueSerde(statsSerde))
                                .toStream()
                                .filter((key, value) -> value != null)
                                .to("transaction-stats");

                // 7. 風控系統整合
                GlobalKTable<String, RiskRule> riskRules = streamsBuilder.globalTable(
                                "risk-rules",
                                Consumed.with(Serdes.String(), new JsonSerde<>(RiskRule.class)));

                transactionStream
                                .leftJoin(
                                                riskRules,
                                                (txnKey, transaction) -> transaction.getType().toString(),
                                                (Transaction transaction, RiskRule rule) -> {
                                                        if (rule != null) {
                                                                transaction.setRiskScore(BigDecimal.valueOf(
                                                                                rule.calculateRisk(transaction)));
                                                        } else {
                                                                transaction.setRiskScore(BigDecimal.ZERO);
                                                        }
                                                        return transaction;
                                                })
                                .to("risk-evaluated-transactions");

                // 8. 交易時間線
                transactionStream
                                .transformValues(new TransactionTimelineTransformer(),
                                                "transaction-timeline-store")
                                .filter((key, value) -> value != null)
                                .mapValues((readOnlyKey, value) -> {
                                        // 確保序列化時使用正確的類型
                                        TransactionTimeline timeline = new TransactionTimeline();
                                        timeline.setCustomerId(value.getCustomerId());
                                        timeline.setTransactions(value.getTransactions());
                                        return timeline;
                                })
                                .to("transaction-timeline",
                                                Produced.with(Serdes.String(),
                                                                new JsonSerde<>(TransactionTimeline.class)));

                // 9. 監控
                transactionStream.peek((key, value) -> {
                        try {
                                MetricsReporter.recordTransaction(value);
                        } catch (Exception e) {
                                log.error("【處理錯誤】交易處理失敗 \n" +
                                                "├─ 錯誤訊息: {} \n" +
                                                "├─ 交易編號: {} \n" +
                                                "└─ 客戶ID: {}",
                                                e.getMessage(),
                                                value.getTransactionId(),
                                                value.getCustomerId(),
                                                e);
                        }
                });

                return transactionStream;
        }

        private <T> void configureJsonSerde(JsonSerde<T> serde) {
                serde.deserializer().configure(Map.of(
                                JsonDeserializer.TRUSTED_PACKAGES, "*",
                                JsonDeserializer.USE_TYPE_INFO_HEADERS, false), false);
        }
}
