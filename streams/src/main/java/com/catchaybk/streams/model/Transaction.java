package com.catchaybk.streams.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    // 交易基本信息
    private String transactionId; // 交易唯一標識
    private String customerId; // 客戶ID
    private TransactionType type; // 交易類型
    private BigDecimal amount; // 交易金額
    private String currency; // 貨幣類型

    // 交易時間信息
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp; // 交易時間
    private String timezone; // 時區

    // 交易狀態
    private TransactionStatus status; // 交易狀態
    private boolean isHighRisk; // 是否高風險交易
    private boolean needsApproval; // 是否需要審批
    private BigDecimal riskScore; // 風險評分

    // 帳戶信息
    private String sourceAccountId; // 來源帳戶
    private String destinationAccountId; // 目標帳戶
    private String sourceAccountType; // 來源帳戶類型
    private String destinationAccountType; // 目標帳戶類型

    // 其他信息
    private String description; // 交易描述
    private String reference; // 交易參考號
    private String category; // 交易類別
    private Map<String, String> metadata; // 額外元數據

    // 審計信息
    private String createdBy; // 創建人
    private LocalDateTime createdAt; // 創建時間
    private String lastModifiedBy; // 最後修改人
    private LocalDateTime lastModifiedAt; // 最後修改時間

    // 業務方法
    public boolean isLargeTransaction() {
        return amount.compareTo(new BigDecimal("1000000")) >= 0;
    }

    public boolean needsAdditionalVerification() {
        return isHighRisk || isLargeTransaction();
    }

    public void markAsProcessed() {
        this.status = TransactionStatus.COMPLETED;
        this.lastModifiedAt = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.metadata.put("failureReason", reason);
        this.lastModifiedAt = LocalDateTime.now();
    }

    // 交易類型枚舉
    public enum TransactionType {
        DEPOSIT, // 存款
        WITHDRAWAL, // 取款
        TRANSFER, // 轉帳
        PAYMENT, // 支付
        REFUND, // 退款
        FEE, // 手續費
        INTEREST, // 利息
        ADJUSTMENT // 調整
    }

    // 交易狀態枚舉
    public enum TransactionStatus {
        PENDING, // 待處理
        PROCESSING, // 處理中
        COMPLETED, // 已完成
        FAILED, // 失敗
        CANCELLED, // 已取消
        REJECTED, // 已拒絕
        REVERSED, // 已撤銷
        SENT // 已發送
    }

    // 建構器方法
    public static class TransactionBuilder {
        // 可以添加一些便利的建構方法
        public TransactionBuilder generateId() {
            this.transactionId = UUID.randomUUID().toString();
            return this;
        }

        public TransactionBuilder withCurrentTimestamp() {
            this.timestamp = LocalDateTime.now();
            return this;
        }
    }
}
