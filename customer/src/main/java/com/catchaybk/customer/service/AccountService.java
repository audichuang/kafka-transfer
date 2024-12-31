package com.catchaybk.customer.service;

import com.catchaybk.customer.entity.CustomerAccount;
import com.catchaybk.customer.repository.CustomerAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final CustomerAccountRepository accountRepository;

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