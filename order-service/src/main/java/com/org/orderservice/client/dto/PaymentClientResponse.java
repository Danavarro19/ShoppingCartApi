package com.org.orderservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentClientResponse {
    private String orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    private LocalDateTime processedAt;
}
