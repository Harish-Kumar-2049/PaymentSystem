package com.example.PaymentSystem.service;

import com.example.PaymentSystem.entity.Wallet;
import com.example.PaymentSystem.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationService {

    private final WalletRepository walletRepository;
    private final LedgerService ledgerService;
    private final AuditLogService auditLogService;

    @Scheduled(cron = "0 0 2 * * *") // runs every day at 2:00 AM
    public void reconcileAllWallets() {
        log.info("Reconciliation job started");
        List<Wallet> wallets = walletRepository.findAll();
        int flagged = 0;

        for (Wallet wallet : wallets) {
            boolean isClean = ledgerService.reconcile(
                wallet.getId(), wallet.getBalance());

            if (!isClean) {
                flagged++;
                log.error(
                    "RECONCILIATION MISMATCH — walletId: {}, " +
                    "current balance: {}",
                    wallet.getId(), wallet.getBalance());

                auditLogService.log(
                    null,
                    null,
                    "RECONCILIATION_MISMATCH",
                    "WALLET",
                    wallet.getBalance().toString(),
                    "MISMATCH_DETECTED"
                );
            }
        }

        log.info("Reconciliation complete. Wallets checked: {}. " +
                 "Flagged: {}", wallets.size(), flagged);
    }

    // call this manually to test without waiting for 2 AM
    public void reconcileNow() {
        reconcileAllWallets();
    }
}

