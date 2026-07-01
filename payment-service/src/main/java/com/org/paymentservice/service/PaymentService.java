package com.org.paymentservice.service;

import com.org.paymentservice.dto.PaymentRequest;
import com.org.paymentservice.dto.PaymentResponse;
import com.org.paymentservice.exception.PaymentNotFoundException;
import com.org.paymentservice.model.Payment;
import com.org.paymentservice.model.PaymentStatus;
import com.org.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setProcessedAt(LocalDateTime.now());

        // Simulate success/failure randomly
        boolean success = ThreadLocalRandom.current().nextBoolean();
        payment.setStatus(success ? PaymentStatus.PAID : PaymentStatus.FAILED);

        Payment saved = repository.save(payment);

        return mapToResponse(saved);
    }

    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = repository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order ID: " + orderId));
        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse res = new PaymentResponse();
        res.setOrderId(payment.getOrderId());
        res.setAmount(payment.getAmount());
        res.setMethod(payment.getMethod());
        res.setStatus(payment.getStatus().name());
        res.setProcessedAt(payment.getProcessedAt());
        return res;
    }
}
