package com.example.PaymentSystem.event;

import com.example.PaymentSystem.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishPaymentSuccess(UUID transactionId,
            UUID sourceWalletId, UUID targetWalletId,
            BigDecimal amount) {
        Map<String, Object> event = Map.of(
            "eventType",      "PAYMENT_SUCCESS",
            "transactionId",  transactionId.toString(),
            "sourceWalletId", sourceWalletId.toString(),
            "targetWalletId", targetWalletId.toString(),
            "amount",         amount.toString(),
            "timestamp",      LocalDateTime.now().toString()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PAYMENT_EXCHANGE,
            RabbitMQConfig.PAYMENT_ROUTING_KEY,
            event);
    }

    public void publishRefundSuccess(UUID transactionId,
            BigDecimal amount) {
        Map<String, Object> event = Map.of(
            "eventType",     "REFUND_SUCCESS",
            "transactionId", transactionId.toString(),
            "amount",        amount.toString(),
            "timestamp",     LocalDateTime.now().toString()
        );
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PAYMENT_EXCHANGE,
            RabbitMQConfig.REFUND_ROUTING_KEY,
            event);
    }
}

