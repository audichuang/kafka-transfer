package com.catchaybk.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule {
    private String type;
    private double threshold;
    private int weight;

    public double calculateRisk(Transaction transaction) {
        if (transaction.getAmount().compareTo(BigDecimal.valueOf(threshold)) > 0) {
            return weight;
        }
        return 0.0;
    }
}