package com.org.orderservice.client;

import com.org.orderservice.client.dto.ProductClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productClient", url = "${external.product-service.base-url}")
public interface ProductClient {

    @GetMapping("/products/{id}")
    ProductClientResponse getProductById(@PathVariable("id") Long id);
}
