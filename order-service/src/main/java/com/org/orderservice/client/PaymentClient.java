package com.org.orderservice.client;

import com.org.orderservice.client.dto.PaymentClientRequest;
import com.org.orderservice.client.dto.PaymentClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "paymentClient", url = "${external.payment-service.base-url}")
public interface PaymentClient {

    @PostMapping("/payments")
    PaymentClientResponse processPayment(@RequestBody PaymentClientRequest request);
}
