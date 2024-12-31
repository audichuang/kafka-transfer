package com.catchaybk.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTimeline {
    private String customerId;
    private List<Transaction> transactions = new ArrayList<>();
    private LocalDateTime lastUpdated;

    public TransactionTimeline(String customerId) {
        this.customerId = customerId;
        this.lastUpdated = LocalDateTime.now();
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
        lastUpdated = LocalDateTime.now();
    }
}