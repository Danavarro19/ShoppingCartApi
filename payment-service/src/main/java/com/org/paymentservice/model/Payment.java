package com.org.paymentservice.model;

import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private BigDecimal amount;
    private String method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime processedAt;
}