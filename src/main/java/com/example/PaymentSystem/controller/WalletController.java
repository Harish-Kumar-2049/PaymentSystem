package com.example.PaymentSystem.controller;

import com.example.PaymentSystem.dto.response.WalletResponse;
import com.example.PaymentSystem.entity.User;
import com.example.PaymentSystem.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @RequestParam(defaultValue = "INR") String currency,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        WalletResponse response = walletService.createWallet(user.getId(), currency);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable UUID walletId) {
        return ResponseEntity.ok(walletService.getWallet(walletId));
    }

    @GetMapping("/my-wallets")
    public ResponseEntity<List<WalletResponse>> getMyWallets(
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(walletService.getUserWallets(user.getId()));
    }
}

