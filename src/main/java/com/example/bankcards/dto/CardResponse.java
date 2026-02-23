package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CardResponse {

    private Long id;
    private String maskedNumber;
    private String ownerUsername;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
