package com.example.PaymentSystem.controller;

import com.example.PaymentSystem.dto.request.RefundRequest;
import com.example.PaymentSystem.dto.request.TransferRequest;
import com.example.PaymentSystem.dto.response.TransactionResponse;
import com.example.PaymentSystem.entity.User;
import com.example.PaymentSystem.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TransactionResponse response =
                transactionService.transfer(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(
            @Valid @RequestBody RefundRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TransactionResponse response =
                transactionService.refund(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable UUID transactionId) {
        return ResponseEntity.ok(
                transactionService.getTransaction(transactionId));
    }
}

