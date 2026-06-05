package com.example.PaymentSystem.repository;

import com.example.PaymentSystem.entity.Transaction;
import com.example.PaymentSystem.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    List<Transaction> findBySourceWallet_Id(UUID walletId);

    List<Transaction> findByStatus(TransactionStatus status);
}

