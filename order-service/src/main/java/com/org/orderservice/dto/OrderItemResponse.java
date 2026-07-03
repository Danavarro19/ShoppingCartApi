package com.org.orderservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long productId;
    private String name;
    private Integer quantity;
    private BigDecimal unitPrice;
}
