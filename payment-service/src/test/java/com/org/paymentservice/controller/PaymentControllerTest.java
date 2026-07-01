package com.org.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.paymentservice.dto.PaymentRequest;
import com.org.paymentservice.dto.PaymentResponse;
import com.org.paymentservice.exception.PaymentNotFoundException;
import com.org.paymentservice.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequest request;
    private PaymentResponse response;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest();
        request.setOrderId("test-order-123");
        request.setAmount(new BigDecimal("99.99"));
        request.setMethod("CREDIT_CARD");

        response = new PaymentResponse();
        response.setOrderId("test-order-123");
        response.setAmount(new BigDecimal("99.99"));
        response.setMethod("CREDIT_CARD");
        response.setStatus("PAID");
        response.setProcessedAt(LocalDateTime.now());
    }

    @Test
    void processPayment_shouldReturnCreatedStatus() throws Exception {
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(response.getOrderId()))
                .andExpect(jsonPath("$.amount").value(response.getAmount().doubleValue()))
                .andExpect(jsonPath("$.method").value(response.getMethod()))
                .andExpect(jsonPath("$.status").value(response.getStatus()));
    }

    @Test
    void getPayment_shouldReturnPayment_whenPaymentExists() throws Exception {
        String orderId = "test-order-123";
        when(paymentService.getPaymentByOrderId(orderId)).thenReturn(response);

        mockMvc.perform(get("/payments/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(response.getOrderId()))
                .andExpect(jsonPath("$.amount").value(response.getAmount().doubleValue()))
                .andExpect(jsonPath("$.method").value(response.getMethod()))
                .andExpect(jsonPath("$.status").value(response.getStatus()));
    }

    @Test
    void getPayment_shouldReturnNotFound_whenPaymentDoesNotExist() throws Exception {
        String orderId = "non-existent-order";
        when(paymentService.getPaymentByOrderId(orderId)).thenThrow(new PaymentNotFoundException("Payment not found for order ID: " + orderId));

        mockMvc.perform(get("/payments/{orderId}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Payment Not Found"))
                .andExpect(jsonPath("$.message").value("Payment not found for order ID: " + orderId));
    }
}
