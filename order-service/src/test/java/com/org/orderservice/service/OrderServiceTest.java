package com.org.orderservice.service;

import com.org.orderservice.client.ProductClient;
import com.org.orderservice.client.dto.ProductClientResponse;
import com.org.orderservice.dto.CreateOrderRequest;
import com.org.orderservice.dto.OrderItemRequest;
import com.org.orderservice.model.Order;
import com.org.orderservice.security.UserIdentityResolver;
import com.org.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserIdentityResolver userIdentityResolver;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setup() {
        when(userIdentityResolver.resolveUserIdentity()).thenReturn("alice@example.com");
    }

    @Test
    void getAllOrdersReturnsOnlyOrdersForAuthenticatedUser() {
        String userIdentity = "alice@example.com";

        Order order = new Order();
        order.setId(10L);
        order.setCustomerId(userIdentity);
        order.setCreatedAt(LocalDateTime.of(2026, 7, 2, 14, 30));
        order.setItems(List.of());
        order.setTotalAmount(BigDecimal.valueOf(22.50));
        when(orderRepository.findByCustomerId(userIdentity)).thenReturn(List.of(order));

        var orders = orderService.getAllOrders();

        assertEquals(1, orders.size());
        assertEquals(userIdentity, orders.get(0).getCustomerId());
        assertEquals(10L, orders.get(0).getId());
        verify(orderRepository).findByCustomerId(userIdentity);
    }

    @Test
    void createOrderIncludesItemNameInResponse() {
        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);
        request.setItems(List.of(itemRequest));

        ProductClientResponse product = new ProductClientResponse();
        product.setId(1L);
        product.setTitle("Laptop");
        product.setPrice(99.99);
        when(productClient.getProductById(1L)).thenReturn(product);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        var response = orderService.createOrder(request);

        assertEquals(1, response.getItems().size());
        assertEquals(1L, response.getItems().get(0).getProductId());
        assertEquals("Laptop", response.getItems().get(0).getName());
        assertEquals(BigDecimal.valueOf(99.99), response.getItems().get(0).getUnitPrice());
        assertEquals(BigDecimal.valueOf(199.98), response.getTotalAmount());
    }
}
