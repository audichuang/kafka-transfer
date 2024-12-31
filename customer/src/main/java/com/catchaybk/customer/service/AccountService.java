package com.catchaybk.customer.service;

import com.catchaybk.customer.entity.CustomerAccount;
import com.catchaybk.customer.repository.CustomerAccountRepository;
import com.catchaybk.streams.model.Transaction;

import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final CustomerAccountRepository accountRepository;
    private final LoggingService loggingService;

    @Transactional
    public void processTransaction(String customerId, BigDecimal amount, boolean isDeposit) {
        CustomerAccount account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("找不到客戶帳戶"));

        try {
            if (isDeposit) {
                account.deposit(amount);
            } else {
                account.withdraw(amount);
            }
            accountRepository.save(account);
        } catch (Exception e) {
            throw new RuntimeException("交易處理失敗: " + e.getMessage());
        }
    }
}