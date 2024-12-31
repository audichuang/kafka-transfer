package com.catchaybk.streams.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RiskRule {
    private String transactionType;
    private double baseRisk = 10.0; // 基礎風險分數
    private double amountMultiplier = 1.0; // 金額風險乘數
    private double timeMultiplier = 1.0; // 時間風險乘數

    public RiskRule() {
        this.baseRisk = 10.0;
        this.amountMultiplier = 1.0;
        this.timeMultiplier = 1.0;
    }

    public RiskRule(String transactionType) {
        this();
        this.transactionType = transactionType;
    }

    public RiskRule(String transactionType, double baseRisk, double amountMultiplier) {
        this.transactionType = transactionType;
        this.baseRisk = baseRisk;
        this.amountMultiplier = amountMultiplier;
        this.timeMultiplier = 1.0;
    }

    public double calculateRisk(Transaction transaction) {
        double riskScore = baseRisk;

        // 使用對數函數計算金額風險，使分數更平滑
        double amountInTenThousand = transaction.getAmount().doubleValue() / 10000;
        double amountRisk = Math.log10(amountInTenThousand + 1) * 10 * amountMultiplier;
        riskScore += amountRisk;

        // 根據交易類型調整風險
        double typeRiskFactor;
        switch (transaction.getType()) {
            case TRANSFER:
                typeRiskFactor = 1.3;
                break;
            case WITHDRAWAL:
                typeRiskFactor = 1.2;
                break;
            case PAYMENT:
                typeRiskFactor = 1.1;
                break;
            default:
                typeRiskFactor = 1.0;
        }
        riskScore *= typeRiskFactor;

        // 如果是高風險標記的交易
        if (transaction.isHighRisk()) {
            riskScore *= 1.5; // 高風險標記的影響降低
        }

        // 加入時間因素（如果是夜間交易，增加風險）
        int hour = transaction.getTimestamp().getHour();
        if (hour >= 23 || hour <= 5) {
            riskScore *= (1 + timeMultiplier * 0.2); // 夜間交易增加風險
        }

        // 確保分數在 0-100 之間，並四捨五入到小數點後一位
        return Math.min(100.0, Math.round(riskScore * 10.0) / 10.0);
    }
}