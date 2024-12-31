package com.catchaybk.streams.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class CustomerBalance {
    private BigDecimal balance = BigDecimal.ZERO;
    private List<Transaction> recentTransactions = new ArrayList<>();

    public CustomerBalance updateBalance(Transaction transaction) {
        if (transaction.getType() == Transaction.TransactionType.DEPOSIT) {
            balance = balance.add(transaction.getAmount());
        } else if (transaction.getType() == Transaction.TransactionType.WITHDRAWAL) {
            balance = balance.subtract(transaction.getAmount());
        }

        recentTransactions.add(transaction);
        if (recentTransactions.size() > 10) {
            recentTransactions.remove(0);
        }
        return this;
    }
}
