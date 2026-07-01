package com.org.paymentservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private String orderId;
    private BigDecimal amount;
    private String method; // e.g., CREDIT_CARD, PAYPAL, etc.
}