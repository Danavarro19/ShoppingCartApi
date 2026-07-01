package com.org.paymentservice.controller;

import com.org.paymentservice.dto.PaymentRequest;
import com.org.paymentservice.dto.PaymentResponse;
import com.org.paymentservice.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody @Valid PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.processPayment(request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable String orderId) {
        return ResponseEntity.ok(service.getPaymentByOrderId(orderId));
    }
}