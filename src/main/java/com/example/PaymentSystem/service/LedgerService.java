package com.example.PaymentSystem.service;

import com.example.PaymentSystem.entity.LedgerEntry;
import com.example.PaymentSystem.entity.Transaction;
import com.example.PaymentSystem.entity.Wallet;
import com.example.PaymentSystem.enums.LedgerEntryType;
import com.example.PaymentSystem.exception.ResourceNotFoundException;
import com.example.PaymentSystem.repository.LedgerEntryRepository;
import com.example.PaymentSystem.repository.TransactionRepository;
import com.example.PaymentSystem.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public void recordDebit(UUID walletId, UUID transactionId,
                             BigDecimal amount, BigDecimal balanceAfter) {
        LedgerEntry entry = buildEntry(walletId, transactionId, amount, balanceAfter, LedgerEntryType.DEBIT);
        ledgerEntryRepository.save(entry);
    }

    public void recordCredit(UUID walletId, UUID transactionId,
                              BigDecimal amount, BigDecimal balanceAfter) {
        LedgerEntry entry = buildEntry(walletId, transactionId, amount, balanceAfter, LedgerEntryType.CREDIT);
        ledgerEntryRepository.save(entry);
    }

    public boolean reconcile(UUID walletId, BigDecimal currentBalance) {
        BigDecimal credits = ledgerEntryRepository.sumCreditsByWalletId(walletId);
        BigDecimal debits = ledgerEntryRepository.sumDebitsByWalletId(walletId);
        BigDecimal expected = credits.subtract(debits);
        return expected.compareTo(currentBalance) == 0;
    }

    private LedgerEntry buildEntry(UUID walletId, UUID transactionId, BigDecimal amount,
                                   BigDecimal balanceAfter, LedgerEntryType type) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        LedgerEntry entry = new LedgerEntry();
        entry.setWallet(wallet);
        entry.setTransaction(transaction);
        entry.setEntryType(type);
        entry.setAmount(amount);
        entry.setBalanceAfter(balanceAfter);
        return entry;
    }
}

