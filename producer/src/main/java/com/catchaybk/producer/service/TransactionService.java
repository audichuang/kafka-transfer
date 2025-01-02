package com.catchaybk.producer.service;

import com.catchaybk.producer.entity.Customer;
import com.catchaybk.producer.repository.CustomerRepository;
import com.catchaybk.streams.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final CustomerRepository customerRepository;
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final Random random = new Random();

    public void generateSingleTransaction() {
        List<Customer> customers = customerRepository.findAll();
        if (!customers.isEmpty()) {
            Customer randomCustomer = customers.get(random.nextInt(customers.size()));
            Transaction transaction = generateRandomTransaction(randomCustomer);
            sendTransaction(transaction);
        }
    }

    public void simulateMultipleTransactions() {
        customerRepository.findAll().forEach(customer -> {
            if (random.nextDouble() < 0.3) { // 30%機率產生交易
                Transaction transaction = generateRandomTransaction(customer);
                sendTransaction(transaction);
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

    private void sendTransaction(Transaction transaction) {
        log.info("【交易產生】準備發送 \n" +
                "├─ 交易編號: {} \n" +
                "├─ 客戶ID: {} \n" +
                "├─ 交易類型: {} \n" +
                "└─ 交易金額: {}",
                transaction.getTransactionId(),
                transaction.getCustomerId(),
                transaction.getType(),
                transaction.getAmount());

//        // 先發送日誌記錄交易產生
//        kafkaTemplate.send("transaction-logs", transaction.getCustomerId(),
//                transaction.toBuilder()
//                        .status(Transaction.TransactionStatus.PENDING)
//                        .build());

        kafkaTemplate.send("transactions", transaction.getCustomerId(), transaction)
                .thenAccept(result -> {
                    log.info("【交易發送】成功 - 交易編號: {}",
                            transaction.getTransactionId());
                    // 記錄發送成功的日誌
                    kafkaTemplate.send("transaction-logs", transaction.getCustomerId(),
                            transaction.toBuilder()
                                    .status(Transaction.TransactionStatus.SENT)
                                    .build());
                })
                .exceptionally(ex -> {
                    log.error("【交易發送】失敗 \n" +
                            "├─ 交易編號: {} \n" +
                            "└─ 錯誤訊息: {}",
                            transaction.getTransactionId(),
                            ex.getMessage());
                    // 發送失敗交易日誌
                    transaction.setStatus(Transaction.TransactionStatus.FAILED);
                    kafkaTemplate.send("transaction-logs",
                            transaction.getCustomerId(),
                            transaction);
                    return null;
                });
    }
}