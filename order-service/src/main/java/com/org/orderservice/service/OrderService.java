package com.org.orderservice.service;

import com.org.orderservice.dto.CreateOrderRequest;
import com.org.orderservice.dto.OrderItemRequest;
import com.org.orderservice.dto.OrderResponse;
import com.org.orderservice.dto.OrderItemResponse;
import com.org.orderservice.exception.OrderNotFoundException;
import com.org.orderservice.model.Order;
import com.org.orderservice.model.OrderItem;
import com.org.orderservice.repository.OrderRepository;
import com.org.orderservice.security.UserIdentityResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final UserIdentityResolver userIdentityResolver;

    public OrderService(OrderRepository orderRepository, UserIdentityResolver userIdentityResolver) {
        this.orderRepository = orderRepository;
        this.userIdentityResolver = userIdentityResolver;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        String userIdentity = userIdentityResolver.resolveUserIdentity();
        log.info("Create order requested by user: {}", userIdentity);

        Order order = new Order();
        order.setCustomerId(userIdentity);
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

    public List<OrderResponse> getAllOrders() {
        return List.of(
                createHardcodedOrder(
                        1L,
                        "customer-1001",
                        LocalDateTime.of(2026, 7, 1, 9, 15),
                        List.of(
                                createItem(101L, 2, BigDecimal.valueOf(9.99)),
                                createItem(102L, 1, BigDecimal.valueOf(14.99))
                        ),
                        BigDecimal.valueOf(34.97)
                ),
                createHardcodedOrder(
                        2L,
                        "customer-1002",
                        LocalDateTime.of(2026, 7, 2, 14, 30),
                        List.of(createItem(103L, 3, BigDecimal.valueOf(7.50))),
                        BigDecimal.valueOf(22.50)
                )
        );
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

    private OrderResponse createHardcodedOrder(Long id, String customerId, LocalDateTime createdAt,
                                               List<OrderItemResponse> items, BigDecimal totalAmount) {
        OrderResponse response = new OrderResponse();
        response.setId(id);
        response.setCustomerId(customerId);
        response.setCreatedAt(createdAt);
        response.setItems(items);
        response.setTotalAmount(totalAmount);
        return response;
    }

    private OrderItemResponse createItem(Long productId, Integer quantity, BigDecimal unitPrice) {
        OrderItemResponse item = new OrderItemResponse();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }

    private BigDecimal fetchPriceFromProductService(Long productId) {
        return BigDecimal.valueOf(9.99); // Simulated price fetch
    }
}
