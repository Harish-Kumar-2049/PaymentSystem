package com.example.PaymentSystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RefundRequest {
    @NotNull
    private UUID transactionId;

    @NotBlank
    private String reason;
}

