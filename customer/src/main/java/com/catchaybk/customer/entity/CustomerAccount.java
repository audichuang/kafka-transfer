package com.catchaybk.customer.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.catchaybk.customer.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer_accounts")
public class CustomerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;
    private String name;
    private BigDecimal balance;

    @Version
    private Long version;

    private LocalDateTime lastTransactionTime;

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.lastTransactionTime = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) throws InsufficientBalanceException {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("餘額不足");
        }
        this.balance = this.balance.subtract(amount);
        this.lastTransactionTime = LocalDateTime.now();
    }
}