package com.example.PaymentSystem.repository;

import com.example.PaymentSystem.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByWallet_Id(UUID walletId);

    List<LedgerEntry> findByTransaction_Id(UUID transactionId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.wallet.id = :walletId AND e.entryType = 'CREDIT'")
    BigDecimal sumCreditsByWalletId(@Param("walletId") UUID walletId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM LedgerEntry e WHERE e.wallet.id = :walletId AND e.entryType = 'DEBIT'")
    BigDecimal sumDebitsByWalletId(@Param("walletId") UUID walletId);
}

