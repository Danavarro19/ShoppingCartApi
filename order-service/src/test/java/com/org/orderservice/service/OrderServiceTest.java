package com.org.orderservice.service;

import com.org.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getAllOrdersReturnsHardcodedOrders() {
        var orders = orderService.getAllOrders();

        assertEquals(2, orders.size());
        assertEquals("customer-1001", orders.get(0).getCustomerId());
        assertNotNull(orders.get(0).getItems());
        assertEquals(2L, orders.get(1).getId());
    }
}
