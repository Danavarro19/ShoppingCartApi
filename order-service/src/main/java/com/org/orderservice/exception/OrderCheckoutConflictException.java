package com.org.orderservice.exception;

public class OrderCheckoutConflictException extends RuntimeException {
    public OrderCheckoutConflictException(String message) {
        super(message);
    }
}
