package com.org.orderservice.dto;

import com.org.orderservice.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutOrderRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
