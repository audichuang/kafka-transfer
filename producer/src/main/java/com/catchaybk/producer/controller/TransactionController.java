package com.catchaybk.producer.controller;

import com.catchaybk.producer.service.TransactionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {
    private final TransactionProducer producer;

    @PostMapping("/generate")
    public String generateTransaction() {
        log.info("Received request to generate transaction");
        try {
            producer.sendTransaction();
            log.info("Transaction sent successfully");
            return "Transaction sent successfully";
        } catch (Exception e) {
            log.error("Error generating transaction", e);
            throw e;
        }
    }
}