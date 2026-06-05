package com.example.PaymentSystem.service;

import com.example.PaymentSystem.entity.AuditLog;
import com.example.PaymentSystem.entity.Transaction;
import com.example.PaymentSystem.entity.User;
import com.example.PaymentSystem.exception.ResourceNotFoundException;
import com.example.PaymentSystem.repository.AuditLogRepository;
import com.example.PaymentSystem.repository.TransactionRepository;
import com.example.PaymentSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    public static final String SYSTEM_USER_EMAIL = "system@payment.internal";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public void log(UUID userId, UUID transactionId,
                    String action, String entityType,
                    String oldValue, String newValue) {
        User user = resolveUser(userId);

        AuditLog log = new AuditLog();
        log.setUser(user);
        if (transactionId != null) {
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
            log.setTransaction(transaction);
        }
        log.setAction(action);
        log.setEntityType(entityType);
        log.setDetails(formatDetails(oldValue, newValue));
        auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByUser(UUID userId) {
        return auditLogRepository.findByUser_Id(userId);
    }

    public List<AuditLog> getLogsByTransaction(UUID transactionId) {
        return auditLogRepository.findByTransaction_Id(transactionId);
    }

    private User resolveUser(UUID userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        }

        return userRepository.findByEmail(SYSTEM_USER_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("System user not found"));
    }

    private String formatDetails(String oldValue, String newValue) {
        if (oldValue == null && newValue == null) {
            return null;
        }
        if (oldValue == null) {
            return "new=" + newValue;
        }
        if (newValue == null) {
            return "old=" + oldValue;
        }
        return "old=" + oldValue + ", new=" + newValue;
    }
}

