package com.example.PaymentSystem.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitMQConfig {

    // queue names
    public static final String PAYMENT_QUEUE       = "payment.processed";
    public static final String REFUND_QUEUE        = "refund.initiated";
    public static final String NOTIFICATION_QUEUE  = "notification.queue";
    public static final String DLQ_PAYMENT         = "payment.processed.dlq";

    // exchange names
    public static final String PAYMENT_EXCHANGE    = "payment.exchange";
    public static final String DLQ_EXCHANGE        = "dlq.exchange";

    // routing keys
    public static final String PAYMENT_ROUTING_KEY = "payment.success";
    public static final String REFUND_ROUTING_KEY  = "refund.success";

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", DLQ_PAYMENT)
            .build();
    }

    @Bean
    public Queue refundQueue() {
        return QueueBuilder.durable(REFUND_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_PAYMENT).build();
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder
            .bind(paymentQueue())
            .to(paymentExchange())
            .with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Binding refundBinding() {
        return BindingBuilder
            .bind(refundQueue())
            .to(paymentExchange())
            .with(REFUND_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory factory) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}

