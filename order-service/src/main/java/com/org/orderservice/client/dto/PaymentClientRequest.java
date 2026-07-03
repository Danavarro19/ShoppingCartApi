package com.org.orderservice.client.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentClientRequest {
    private String orderId;
    private BigDecimal amount;
    private String method;
}
