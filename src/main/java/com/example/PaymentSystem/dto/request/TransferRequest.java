package com.example.PaymentSystem.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransferRequest {
    @NotNull
    private UUID sourceWalletId;

    @NotNull
    private UUID targetWalletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private UUID idempotencyKey;
}

