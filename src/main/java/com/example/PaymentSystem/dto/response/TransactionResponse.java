package com.example.PaymentSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String type;
    private String status;
    private UUID sourceWalletId;
    private UUID targetWalletId;
    private LocalDateTime createdAt;
}

