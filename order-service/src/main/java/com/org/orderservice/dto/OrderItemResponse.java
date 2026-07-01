package com.org.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
}