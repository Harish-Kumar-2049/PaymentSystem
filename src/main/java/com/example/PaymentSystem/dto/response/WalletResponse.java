package com.example.PaymentSystem.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WalletResponse {
    private UUID id;
    private BigDecimal balance;
    private String currency;
    private String status;
    private boolean isPrimary;
    private LocalDateTime createdAt;
}

