package com.example.bankcards.dto;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class ErrorResponse {

    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
