package com.org.orderservice.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;
}
