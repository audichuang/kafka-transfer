package com.catchaybk.logger.config;

import com.catchaybk.streams.model.Transaction;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka消費者配置類
 * 用於配置日誌服務的Kafka消費者相關設置
 */
@Configuration
public class KafkaConfig {

    /**
     * 創建Kafka消費者工廠
     * 配置消費者的基本屬性，包括：
     * - 服務器地址
     * - 消費者組ID
     * - 序列化器設置
     * - 信任包設置
     */
    @Bean
    public ConsumerFactory<String, Transaction> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // 設置Kafka服務器地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 設置消費者組ID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "logger-consumer");
        // 設置鍵的反序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        // 設置值的反序列化器
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // 配置信任的包，允許反序列化所有包
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        // 設置默認的值類型
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, Transaction.class.getName());

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 創建Kafka監聽器容器工廠
     * 用於處理@KafkaListener註解的消息監聽
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Transaction> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Transaction> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}