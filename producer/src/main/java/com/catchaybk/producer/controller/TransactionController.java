package com.catchaybk.producer.controller;

import com.catchaybk.producer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/generate/single")
    public String generateSingleTransaction() {
        log.info("收到單筆交易生成請求");
        try {
            transactionService.generateSingleTransaction();
            return "交易已發送";
        } catch (Exception e) {
            log.error("生成交易時發生錯誤", e);
            throw e;
        }
    }

    @PostMapping("/generate/multiple")
    public String generateMultipleTransactions() {
        log.info("收到多筆交易生成請求");
        try {
            transactionService.simulateMultipleTransactions();
            return "多筆交易已發送";
        } catch (Exception e) {
            log.error("生成多筆交易時發生錯誤", e);
            throw e;
        }
    }
}