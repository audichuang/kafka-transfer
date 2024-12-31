package com.catchaybk.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Alert {
    private String message;
    private String severity;
    private LocalDateTime timestamp;
    
    public Alert(String message) {
        this.message = message;
        this.severity = "HIGH";
        this.timestamp = LocalDateTime.now();
    }
}
