package com.example.PaymentSystem.repository;

import com.example.PaymentSystem.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUser_Id(UUID userId);

    List<AuditLog> findByTransaction_Id(UUID transactionId);

    List<AuditLog> findByEntityType(String entityType);
}

