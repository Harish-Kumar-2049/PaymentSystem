package com.example.PaymentSystem.event;

import com.example.PaymentSystem.config.RabbitMQConfig;
import com.example.PaymentSystem.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final AuditLogService auditLogService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentSuccess(Map<String, Object> event) {
        log.info("Payment event received: {}", event);
        // notification stub — in real world sends email/SMS
        log.info("Notification: Transfer of {} completed. TxnId: {}",
            event.get("amount"),
            event.get("transactionId"));
    }

    @RabbitListener(queues = RabbitMQConfig.REFUND_QUEUE)
    public void handleRefundSuccess(Map<String, Object> event) {
        log.info("Refund event received: {}", event);
        log.info("Notification: Refund of {} processed. TxnId: {}",
            event.get("amount"),
            event.get("transactionId"));
    }

    @RabbitListener(queues = RabbitMQConfig.DLQ_PAYMENT)
    public void handleDeadLetter(Map<String, Object> event) {
        log.error("DEAD LETTER — failed to process event: {}", event);
        // in production: alert ops team, store for manual review
    }
}

