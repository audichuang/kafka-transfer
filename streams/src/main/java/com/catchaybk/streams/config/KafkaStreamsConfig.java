package com.catchaybk.streams.config;

import com.catchaybk.streams.model.*;
import com.catchaybk.streams.transform.MetricsReporter;
import com.catchaybk.streams.transform.TransactionTimelineTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
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
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Streams 配置類
 * 負責設置和配置所有 Kafka Streams 相關的處理邏輯
 */
@Configuration
@EnableKafkaStreams
@Slf4j
public class KafkaStreamsConfig {

        /**
         * 配置 Kafka Streams 的基本屬性
         * 包括應用ID、服務器地址、序列化配置等
         */
        @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
        public KafkaStreamsConfiguration kStreamsConfig() {
                Map<String, Object> props = new HashMap<>();

                // 設置應用ID，用於識別此Streams應用
                props.put(StreamsConfig.APPLICATION_ID_CONFIG, "advanced-transaction-processor");
                // 設置Kafka服務器地址
                props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
                // 設置異常處理器，發生錯誤時記錄並繼續
                props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                                LogAndContinueExceptionHandler.class);
                // 設置消費者偏移量重置策略
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                // 啟用精確一次性處理語義
                props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
                // 設置處理線程數
                props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1);
                // 設置緩存大小（10MB）
                props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
                // 設置提交間隔（毫秒）
                props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);
                // 設置默認的鍵序列化器
                props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
                // 設置默認的值序列化器
                props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);

                return new KafkaStreamsConfiguration(props);
        }

        /**
         * 配置 Kafka Streams 處理邏輯
         * 定義了整個交易處理流程的拓撲結構
         */
        @Bean
        public KStream<String, Transaction> kStream(StreamsBuilder streamsBuilder) {
                // 配置各種序列化器
                JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
                JsonSerde<TransactionStats> statsSerde = new JsonSerde<>(TransactionStats.class);
                JsonSerde<CustomerBalance> balanceSerde = new JsonSerde<>(CustomerBalance.class);
                JsonSerde<TransactionTimeline> timelineSerde = new JsonSerde<>(TransactionTimeline.class);

                // 配置交易時間軸的狀態存儲
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

                // 2. 分離大額交易和普通交易
                KStream<String, Transaction>[] branches = transactionStream.branch(
                                // 條件1：金額大於等於100萬的為大額交易
                                (key, transaction) -> transaction.getAmount().compareTo(new BigDecimal("1000000")) >= 0,
                                // 條件2：其他所有交易
                                (key, transaction) -> true);

                KStream<String, Transaction> largeTransactions = branches[0]; // 大額交易流
                KStream<String, Transaction> normalTransactions = branches[1]; // 普通交易流

                // 3. 處理大額交易
                largeTransactions
                                .mapValues(transaction -> {
                                        transaction.setHighRisk(true); // 標記為高風險
                                        transaction.setNeedsApproval(true); // 需要審批
                                        return transaction;
                                })
                                .to("large-transactions"); // 發送到大額交易主題

                // 4. 計算客戶餘額
                normalTransactions
                                .groupByKey() // 按客戶ID分組
                                .aggregate(
                                                CustomerBalance::new, // 初始化餘額
                                                (key, transaction, balance) -> balance.updateBalance(transaction), // 更新餘額
                                                Materialized.<String, CustomerBalance, KeyValueStore<Bytes, byte[]>>as(
                                                                "customer-balance-store") // 存儲餘額狀態
                                                                .withKeySerde(Serdes.String())
                                                                .withValueSerde(balanceSerde));

                // 5. 檢測可疑交易（5分鐘內超過5筆交易）
                normalTransactions
                                .groupByKey()
                                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
                                .count()
                                .toStream()
                                .filter((key, count) -> count >= 5) // 篩選出頻繁交易
                                .mapValues(count -> new Alert("Suspicious Activity Detected"))
                                .to("suspicious-transactions");

                // 6. 每小時交易統計
                normalTransactions
                                .groupByKey()
                                .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                                .aggregate(
                                                TransactionStats::new, // 初始化統計
                                                (key, transaction, stats) -> stats.updateStats(transaction), // 更新統計
                                                Materialized.<String, TransactionStats, WindowStore<Bytes, byte[]>>as(
                                                                "hourly-stats")
                                                                .withKeySerde(Serdes.String())
                                                                .withValueSerde(statsSerde))
                                .toStream()
                                .filter((key, value) -> value != null)
                                .to("transaction-stats");

                // 7. 風險評估系統整合
                GlobalKTable<String, RiskRule> riskRules = streamsBuilder.globalTable(
                                "risk-rules", // 風險規則主題
                                Consumed.with(Serdes.String(), new JsonSerde<>(RiskRule.class)));

                // 將交易與風險規則關聯並計算風險分數
                transactionStream
                                .leftJoin(
                                                riskRules,
                                                (txnKey, transaction) -> transaction.getType().toString(),
                                                (transaction, rule) -> {
                                                        if (rule != null) {
                                                                transaction.setRiskScore(BigDecimal.valueOf(
                                                                                rule.calculateRisk(transaction)));
                                                        } else {
                                                                transaction.setRiskScore(BigDecimal.ZERO);
                                                        }
                                                        return transaction;
                                                })
                                .to("risk-evaluated-transactions");

                // 8. 維護交易時間軸
                transactionStream
                                .transformValues(new TransactionTimelineTransformer(),
                                                "transaction-timeline-store")
                                .filter((key, value) -> value != null)
                                .to("transaction-timeline");

                // 9. 監控指標記錄
                transactionStream.peek((key, value) -> {
                        try {
                                MetricsReporter.recordTransaction(value);
                        } catch (Exception e) {
                                log.error("處理錯誤: {}", e.getMessage(), e);
                        }
                });

                return transactionStream;
        }
}
