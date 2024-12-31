package com.catchaybk.streams.config;

import com.catchaybk.streams.model.RiskRule;
import com.catchaybk.streams.model.Transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RiskRuleInitializer {

    private final KafkaTemplate<String, RiskRule> kafkaTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeRiskRules() {
        // 轉帳交易規則
        RiskRule transferRule = new RiskRule("TRANSFER", 20.0, 1.5);
        kafkaTemplate.send("risk-rules", "TRANSFER", transferRule);

        // 提款交易規則
        RiskRule withdrawalRule = new RiskRule("WITHDRAWAL", 15.0, 1.3);
        kafkaTemplate.send("risk-rules", "WITHDRAWAL", withdrawalRule);

        // 支付交易規則
        RiskRule paymentRule = new RiskRule("PAYMENT", 10.0, 1.2);
        kafkaTemplate.send("risk-rules", "PAYMENT", paymentRule);

        // 存款交易規則
        RiskRule depositRule = new RiskRule("DEPOSIT", 5.0, 1.0);
        kafkaTemplate.send("risk-rules", "DEPOSIT", depositRule);
    }
}