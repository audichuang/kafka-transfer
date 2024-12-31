package com.catchaybk.producer.service;

import com.catchaybk.producer.entity.Customer;
import com.catchaybk.producer.repository.CustomerRepository;
import com.catchaybk.streams.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionSimulator {
    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final Random random = new Random();

    @Scheduled(fixedRate = 3000) // 每10秒產生一筆交易
    public void simulateTransaction() {
        // 隨機選擇一個客戶
        customerRepository.findAll().forEach(customer -> {
            if (random.nextDouble() < 0.9) { // 30%機率產生交易
                Transaction transaction = generateRandomTransaction(customer);
                kafkaTemplate.send("transactions", customer.getCustomerId(), transaction);
            }
        });
    }

    private Transaction generateRandomTransaction(Customer customer) {
        Transaction.TransactionType[] types = Transaction.TransactionType.values();
        Transaction.TransactionType type = types[random.nextInt(types.length)];

        BigDecimal amount = new BigDecimal(random.nextInt(1000000) + 1000);

        return Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .customerId(customer.getCustomerId())
                .type(type)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .status(Transaction.TransactionStatus.PENDING)
                .build();
    }
}