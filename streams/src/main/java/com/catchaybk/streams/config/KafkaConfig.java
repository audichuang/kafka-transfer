package com.catchaybk.streams.config;

import com.catchaybk.streams.model.RiskRule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka生產者配置類
 * 用於配置風險規則相關的Kafka生產者設置
 */
@Configuration
public class KafkaConfig {

    /**
     * 創建風險規則的Kafka模板
     * 用於發送風險規則消息到Kafka
     */
    @Bean
    public KafkaTemplate<String, RiskRule> riskRuleKafkaTemplate(
            ProducerFactory<String, RiskRule> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * 創建Kafka生產者工廠
     * 配置生產者的基本屬性，包括：
     * - 服務器地址
     * - 序列化器設置
     */
    @Bean
    public ProducerFactory<String, RiskRule> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        // 設置Kafka服務器地址
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 設置鍵的序列化器
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 設置值的序列化器
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
}