package com.example.PaymentSystem.service;

import com.example.PaymentSystem.dto.request.RefundRequest;
import com.example.PaymentSystem.dto.request.TransferRequest;
import com.example.PaymentSystem.dto.response.TransactionResponse;
import com.example.PaymentSystem.entity.Transaction;
import com.example.PaymentSystem.entity.Wallet;
import com.example.PaymentSystem.enums.TransactionStatus;
import com.example.PaymentSystem.enums.TransactionType;
import com.example.PaymentSystem.enums.WalletStatus;
import com.example.PaymentSystem.exception.DuplicateTransactionException;
import com.example.PaymentSystem.exception.ResourceNotFoundException;
import com.example.PaymentSystem.repository.TransactionRepository;
import com.example.PaymentSystem.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final AuditLogService auditLogService;

    @Transactional
    public TransactionResponse transfer(TransferRequest request, UUID initiatedByUserId) {
        String idempotencyKey = request.getIdempotencyKey().toString();
        transactionRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(t -> {
                    throw new DuplicateTransactionException(
                            "Transaction already processed with this idempotency key");
                });

        Wallet source = walletRepository.findById(request.getSourceWalletId())
                .filter(w -> w.getStatus() == WalletStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Source wallet not found"));
        Wallet target = walletRepository.findById(request.getTargetWalletId())
                .filter(w -> w.getStatus() == WalletStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Target wallet not found"));

        Transaction txn = new Transaction();
        txn.setIdempotencyKey(idempotencyKey);
        txn.setSourceWallet(source);
        txn.setTargetWallet(target);
        txn.setAmount(request.getAmount());
        txn.setType(TransactionType.TRANSFER);
        txn.setStatus(TransactionStatus.INITIATED);
        Transaction saved = transactionRepository.save(txn);

        try {
            saved.setStatus(TransactionStatus.PENDING);
            transactionRepository.save(saved);

            walletService.debit(source.getId(), request.getAmount());
            walletService.credit(target.getId(), request.getAmount());

            BigDecimal sourceBalanceAfter = walletRepository.findById(source.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Source wallet not found"))
                    .getBalance();
            BigDecimal targetBalanceAfter = walletRepository.findById(target.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target wallet not found"))
                    .getBalance();

            ledgerService.recordDebit(source.getId(), saved.getId(),
                    request.getAmount(), sourceBalanceAfter);
            ledgerService.recordCredit(target.getId(), saved.getId(),
                    request.getAmount(), targetBalanceAfter);

            saved.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(saved);

            auditLogService.log(initiatedByUserId, saved.getId(),
                    "TRANSFER_SUCCESS", "TRANSACTION",
                    TransactionStatus.PENDING.name(),
                    TransactionStatus.SUCCESS.name());

            return mapToResponse(saved);

        } catch (Exception e) {
            saved.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(saved);

            auditLogService.log(initiatedByUserId, saved.getId(),
                    "TRANSFER_FAILED", "TRANSACTION",
                    TransactionStatus.PENDING.name(),
                    TransactionStatus.FAILED.name());

            throw e;
        }
    }

    @Transactional
    public TransactionResponse refund(RefundRequest request, UUID initiatedByUserId) {
        Transaction original = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (original.getStatus() != TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Only successful transactions can be refunded");
        }

        walletService.debit(original.getTargetWallet().getId(), original.getAmount());
        walletService.credit(original.getSourceWallet().getId(), original.getAmount());

        original.setStatus(TransactionStatus.REFUNDED);
        original.setRefundReason(request.getReason());
        transactionRepository.save(original);

        auditLogService.log(initiatedByUserId, original.getId(),
                "REFUND_SUCCESS", "TRANSACTION",
                TransactionStatus.SUCCESS.name(),
                TransactionStatus.REFUNDED.name());

        return mapToResponse(original);
    }

    public TransactionResponse getTransaction(UUID transactionId) {
        Transaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return mapToResponse(txn);
    }

    private TransactionResponse mapToResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .amount(txn.getAmount())
                .type(txn.getType().name())
                .status(txn.getStatus().name())
                .sourceWalletId(txn.getSourceWallet().getId())
                .targetWalletId(txn.getTargetWallet().getId())
                .createdAt(LocalDateTime.ofInstant(txn.getCreatedAt(), ZoneId.systemDefault()))
                .build();
    }
}

