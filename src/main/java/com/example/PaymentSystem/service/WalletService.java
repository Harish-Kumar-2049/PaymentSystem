package com.example.PaymentSystem.service;

import com.example.PaymentSystem.dto.response.WalletResponse;
import com.example.PaymentSystem.entity.User;
import com.example.PaymentSystem.entity.Wallet;
import com.example.PaymentSystem.enums.WalletStatus;
import com.example.PaymentSystem.exception.InsufficientFundsException;
import com.example.PaymentSystem.exception.ResourceNotFoundException;
import com.example.PaymentSystem.repository.UserRepository;
import com.example.PaymentSystem.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public WalletResponse createWallet(UUID userId, String currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setCurrency(currency != null ? currency : "INR");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);

        Wallet saved = walletRepository.save(wallet);
        auditLogService.log(userId, null,
                "WALLET_CREATED", "WALLET", null, saved.getId().toString());

        return mapToResponse(saved);
    }

    public WalletResponse getWallet(UUID walletId) {
        Wallet wallet = walletRepository
                .findByIdAndStatus(walletId, WalletStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found or inactive"));
        return mapToResponse(wallet);
    }

    public List<WalletResponse> getUserWallets(UUID userId) {
        return walletRepository.findByUser_Id(userId)
                .stream().map(this::mapToResponse).toList();
    }

    public void debit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository
                .findByIdAndStatus(walletId, WalletStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Source wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance in wallet");
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    public void credit(UUID walletId, BigDecimal amount) {
        Wallet wallet = walletRepository
                .findByIdAndStatus(walletId, WalletStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Target wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    public void adminDeposit(UUID walletId, BigDecimal amount) {
        credit(walletId, amount);
        auditLogService.log(null, null,
                "ADMIN_DEPOSIT", "WALLET", null,
                "Deposited " + amount + " to wallet " + walletId);
    }

    private WalletResponse mapToResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .createdAt(LocalDateTime.ofInstant(wallet.getCreatedAt(), ZoneId.systemDefault()))
                .build();
    }
}

