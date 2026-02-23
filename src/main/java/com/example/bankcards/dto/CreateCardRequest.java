package com.example.bankcards.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class CreateCardRequest {

    @NotBlank
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotNull
    private Long ownerId;

    @NotNull
    @Future
    private LocalDate expiryDate;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialBalance;
}
