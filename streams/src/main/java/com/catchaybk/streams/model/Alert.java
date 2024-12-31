package com.catchaybk.streams.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private String message;
    private String severity = "HIGH";
    private LocalDateTime timestamp = LocalDateTime.now();

    public Alert(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
