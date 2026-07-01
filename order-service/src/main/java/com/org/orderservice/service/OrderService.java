package com.org.orderservice.service;

import com.org.orderservice.dto.CreateOrderRequest;
import com.org.orderservice.dto.OrderItemRequest;
import com.org.orderservice.dto.OrderResponse;
import com.org.orderservice.dto.OrderItemResponse;
import com.org.orderservice.exception.OrderNotFoundException;
import com.org.orderservice.model.Order;
import com.org.orderservice.model.OrderItem;
import com.org.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = request.getItems().stream().map(req -> {
            OrderItem item = new OrderItem();
            item.setProductId(req.getProductId());
            item.setQuantity(req.getQuantity());
            item.setUnitPrice(fetchPriceFromProductService(req.getProductId())); // stub
            item.setOrder(order);
            return item;
        }).collect(Collectors.toList());

        order.setItems(items);
        order.setTotalAmount(items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setCustomerId(order.getCustomerId());
        res.setCreatedAt(order.getCreatedAt());
        res.setTotalAmount(order.getTotalAmount());
        res.setItems(order.getItems().stream().map(i -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setProductId(i.getProductId());
            r.setQuantity(i.getQuantity());
            r.setUnitPrice(i.getUnitPrice());
            return r;
        }).collect(Collectors.toList()));
        return res;
    }

    private BigDecimal fetchPriceFromProductService(Long productId) {
        return BigDecimal.valueOf(9.99); // Simulated price fetch
    }
}
