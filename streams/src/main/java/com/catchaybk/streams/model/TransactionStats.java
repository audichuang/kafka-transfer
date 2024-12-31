package com.catchaybk.streams.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TransactionStats {
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int count = 0;
    private BigDecimal maxAmount = BigDecimal.ZERO;
    private BigDecimal minAmount = BigDecimal.valueOf(Double.MAX_VALUE);

    public TransactionStats updateStats(Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        totalAmount = totalAmount.add(amount);
        count++;
        maxAmount = maxAmount.max(amount);
        minAmount = minAmount.min(amount);
        return this;
    }
}
