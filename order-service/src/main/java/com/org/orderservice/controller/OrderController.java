package com.org.orderservice.controller;

import com.org.orderservice.dto.CheckoutOrderRequest;
import com.org.orderservice.dto.CreateOrderRequest;
import com.org.orderservice.dto.OrderResponse;
import com.org.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request,
                                                     Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(Principal principal) {
        return ResponseEntity.ok(orderService.getAllOrders(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(orderService.getOrderById(id, principal.getName()));
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<OrderResponse> checkoutOrder(@PathVariable Long id,
                                                       @RequestBody @Valid CheckoutOrderRequest request,
                                                       Principal principal) {
        return ResponseEntity.ok(orderService.checkoutOrder(id, request, principal.getName()));
    }
}
