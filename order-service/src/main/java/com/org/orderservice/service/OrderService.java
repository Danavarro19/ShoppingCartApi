package com.org.orderservice.service;

import com.org.orderservice.client.ProductClient;
import com.org.orderservice.client.dto.ProductClientResponse;
import com.org.orderservice.dto.CreateOrderRequest;
import com.org.orderservice.dto.OrderResponse;
import com.org.orderservice.dto.OrderItemResponse;
import com.org.orderservice.exception.OrderNotFoundException;
import com.org.orderservice.exception.ProductNotFoundException;
import com.org.orderservice.exception.ProductServiceUnavailableException;
import com.org.orderservice.model.Order;
import com.org.orderservice.model.OrderItem;
import com.org.orderservice.model.PaymentStatus;
import com.org.orderservice.repository.OrderRepository;
import com.org.orderservice.security.UserIdentityResolver;
import feign.FeignException;
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
    private final ProductClient productClient;

    public OrderService(
            OrderRepository orderRepository,
            UserIdentityResolver userIdentityResolver,
            ProductClient productClient
    ) {
        this.orderRepository = orderRepository;
        this.userIdentityResolver = userIdentityResolver;
        this.productClient = productClient;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        String userIdentity = userIdentityResolver.resolveUserIdentity();
        log.info("Create order requested by user: {}", userIdentity);

        Order order = new Order();
        order.setCustomerId(userIdentity);
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentStatus(PaymentStatus.PENDING);

        List<OrderItem> items = request.getItems().stream().map(req -> {
            OrderItem item = new OrderItem();
            item.setProductId(req.getProductId());
            item.setQuantity(req.getQuantity());
            ProductClientResponse product = fetchProductFromProductService(req.getProductId());
            item.setName(product.getTitle());
            if (product.getPrice() == null) {
                throw new ProductServiceUnavailableException("Product price is unavailable", null);
            }
            item.setUnitPrice(BigDecimal.valueOf(product.getPrice()));
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
        String userIdentity = userIdentityResolver.resolveUserIdentity();
        log.info("Fetch all orders requested by user: {}", userIdentity);

        return orderRepository.findByCustomerId(userIdentity).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse res = new OrderResponse();
        res.setId(order.getId());
        res.setCustomerId(order.getCustomerId());
        res.setCreatedAt(order.getCreatedAt());
        res.setTotalAmount(order.getTotalAmount());
        res.setPaymentStatus(order.getPaymentStatus());
        res.setItems(order.getItems().stream().map(i -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setProductId(i.getProductId());
            r.setName(i.getName());
            r.setQuantity(i.getQuantity());
            r.setUnitPrice(i.getUnitPrice());
            return r;
        }).collect(Collectors.toList()));
        return res;
    }

    private ProductClientResponse fetchProductFromProductService(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException(productId);
        } catch (FeignException ex) {
            throw new ProductServiceUnavailableException("Unable to fetch product details", ex);
        }
    }
}
