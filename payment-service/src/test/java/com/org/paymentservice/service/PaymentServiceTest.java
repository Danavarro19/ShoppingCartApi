package com.org.paymentservice.service;

import com.org.paymentservice.dto.PaymentRequest;
import com.org.paymentservice.dto.PaymentResponse;
import com.org.paymentservice.exception.PaymentNotFoundException;
import com.org.paymentservice.model.Payment;
import com.org.paymentservice.model.PaymentStatus;
import com.org.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository repository;

    @InjectMocks
    private PaymentService service;

    private PaymentRequest request;
    private Payment payment;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest();
        request.setOrderId("test-order-123");
        request.setAmount(new BigDecimal("99.99"));
        request.setMethod("CREDIT_CARD");

        payment = new Payment();
        payment.setId(1L);
        payment.setOrderId("test-order-123");
        payment.setAmount(new BigDecimal("99.99"));
        payment.setMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.PAID);
        payment.setProcessedAt(LocalDateTime.now());
    }

    @Test
    void processPayment_shouldReturnPaymentResponse() {
        // Arrange
        when(repository.save(any(Payment.class))).thenReturn(payment);

        // Act
        PaymentResponse response = service.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.getOrderId(), response.getOrderId());
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(request.getMethod(), response.getMethod());
        assertEquals(payment.getStatus().name(), response.getStatus());
        assertNotNull(response.getProcessedAt());
        verify(repository, times(1)).save(any(Payment.class));
    }

    @Test
    void getPaymentByOrderId_shouldReturnPaymentResponse_whenPaymentExists() {
        // Arrange
        String orderId = "test-order-123";
        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // Act
        PaymentResponse response = service.getPaymentByOrderId(orderId);

        // Assert
        assertNotNull(response);
        assertEquals(payment.getOrderId(), response.getOrderId());
        assertEquals(payment.getAmount(), response.getAmount());
        assertEquals(payment.getMethod(), response.getMethod());
        assertEquals(payment.getStatus().name(), response.getStatus());
        assertEquals(payment.getProcessedAt(), response.getProcessedAt());
        verify(repository, times(1)).findByOrderId(orderId);
    }

    @Test
    void getPaymentByOrderId_shouldThrowException_whenPaymentDoesNotExist() {
        // Arrange
        String orderId = "non-existent-order";
        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        PaymentNotFoundException exception = assertThrows(PaymentNotFoundException.class, () -> {
            service.getPaymentByOrderId(orderId);
        });
        assertEquals("Payment not found for order ID: " + orderId, exception.getMessage());
        verify(repository, times(1)).findByOrderId(orderId);
    }
}
