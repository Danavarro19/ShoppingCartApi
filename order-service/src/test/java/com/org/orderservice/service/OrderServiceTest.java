package com.org.orderservice.service;

import com.org.orderservice.client.ProductClient;
import com.org.orderservice.model.Order;
import com.org.orderservice.security.UserIdentityResolver;
import com.org.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void getAllOrdersReturnsOnlyOrdersForAuthenticatedUser() {
        String userIdentity = "alice@example.com";
        when(userIdentityResolver.resolveUserIdentity()).thenReturn(userIdentity);

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
}
