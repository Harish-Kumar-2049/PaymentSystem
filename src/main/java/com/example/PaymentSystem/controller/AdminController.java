package com.example.PaymentSystem.controller;

import com.example.PaymentSystem.dto.response.UserWalletsResponse;
import com.example.PaymentSystem.service.WalletService;
import com.example.PaymentSystem.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final WalletService walletService;
    private final ReconciliationService reconciliationService;

    @PostMapping("/wallets/{walletId}/deposit")
    public ResponseEntity<Map<String, String>> deposit(
            @PathVariable UUID walletId,
            @RequestParam BigDecimal amount) {
        walletService.adminDeposit(walletId, amount);
        return ResponseEntity.ok(Map.of(
                "message", "Deposit successful",
                "walletId", walletId.toString(),
                "amount", amount.toString()
        ));
    }

    @GetMapping("/users/lookup")
    public ResponseEntity<UserWalletsResponse> lookupUserWallets(
            @RequestParam String query) {
        return ResponseEntity.ok(walletService.lookupUserWallets(query));
    }

    @PostMapping("/reconcile")
    public ResponseEntity<Map<String, String>> triggerReconciliation() {
        reconciliationService.reconcileNow();
        return ResponseEntity.ok(Map.of(
                "message", "Reconciliation completed — check logs"));
    }
}

