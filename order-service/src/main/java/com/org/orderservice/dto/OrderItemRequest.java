package com.org.orderservice.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class OrderItemRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull(message = "Quantity is required")
    private Integer quantity;
}