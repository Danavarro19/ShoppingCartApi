package com.org.orderservice.service;

import com.org.orderservice.client.PaymentClient;
import com.org.orderservice.client.dto.PaymentClientRequest;
import com.org.orderservice.client.dto.PaymentClientResponse;
import com.org.orderservice.client.ProductClient;
import com.org.orderservice.client.dto.ProductClientResponse;
import com.org.orderservice.dto.CheckoutOrderRequest;
import com.org.orderservice.dto.CreateOrderRequest;
import com.org.orderservice.dto.OrderItemRequest;
import com.org.orderservice.exception.OrderCheckoutConflictException;
import com.org.orderservice.exception.OrderNotFoundException;
import com.org.orderservice.exception.PaymentServiceUnavailableException;
import com.org.orderservice.exception.UnauthorizedException;
import com.org.orderservice.model.Order;
import com.org.orderservice.model.PaymentMethod;
import com.org.orderservice.model.PaymentStatus;
import com.org.orderservice.security.UserIdentityResolver;
import com.org.orderservice.repository.OrderRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Mock
    private PaymentClient paymentClient;

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
        order.setPaymentStatus(PaymentStatus.PENDING);
        when(orderRepository.findByCustomerId(userIdentity)).thenReturn(List.of(order));

        var orders = orderService.getAllOrders();

        assertEquals(1, orders.size());
        assertEquals(userIdentity, orders.get(0).getCustomerId());
        assertEquals(10L, orders.get(0).getId());
        assertEquals(PaymentStatus.PENDING, orders.get(0).getPaymentStatus());
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
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
    }

    @Test
    void createOrderThrowsUnauthorizedWhenUserIsNotAuthenticated() {
        when(userIdentityResolver.resolveUserIdentity()).thenReturn("unknown");

        CreateOrderRequest request = new CreateOrderRequest();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        request.setItems(List.of(itemRequest));

        assertThrows(UnauthorizedException.class, () -> orderService.createOrder(request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderByIdReturnsOrderWhenItBelongsToAuthenticatedUser() {
        Order order = new Order();
        order.setId(11L);
        order.setCustomerId("alice@example.com");
        order.setCreatedAt(LocalDateTime.of(2026, 7, 3, 9, 0));
        order.setItems(List.of());
        order.setTotalAmount(BigDecimal.valueOf(50.00));
        order.setPaymentStatus(PaymentStatus.PENDING);

        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

        var response = orderService.getOrderById(11L);

        assertEquals(11L, response.getId());
        assertEquals("alice@example.com", response.getCustomerId());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
    }

    @Test
    void getOrderByIdThrowsNotFoundWhenOrderBelongsToDifferentUser() {
        Order order = new Order();
        order.setId(12L);
        order.setCustomerId("bob@example.com");

        when(orderRepository.findById(12L)).thenReturn(Optional.of(order));

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(12L));
    }

    @Test
    void checkoutOrderMarksOrderAsPaidWhenPaymentSucceeds() {
        Order order = new Order();
        order.setId(13L);
        order.setCustomerId("alice@example.com");
        order.setCreatedAt(LocalDateTime.of(2026, 7, 3, 9, 15));
        order.setItems(List.of());
        order.setTotalAmount(BigDecimal.valueOf(75.00));
        order.setPaymentStatus(PaymentStatus.PENDING);

        PaymentClientResponse paymentResponse = new PaymentClientResponse();
        paymentResponse.setOrderId("13");
        paymentResponse.setStatus("PAID");

        when(orderRepository.findById(13L)).thenReturn(Optional.of(order));
        when(paymentClient.processPayment(any())).thenReturn(paymentResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.checkoutOrder(13L, checkoutRequest(PaymentMethod.CREDIT_CARD));

        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        ArgumentCaptor<PaymentClientRequest> requestCaptor = ArgumentCaptor.forClass(PaymentClientRequest.class);
        verify(paymentClient).processPayment(requestCaptor.capture());
        assertEquals(PaymentMethod.CREDIT_CARD.name(), requestCaptor.getValue().getMethod());
        verify(orderRepository).save(order);
    }

    @Test
    void checkoutOrderMarksOrderAsPaymentFailedWhenPaymentFails() {
        Order order = new Order();
        order.setId(14L);
        order.setCustomerId("alice@example.com");
        order.setCreatedAt(LocalDateTime.of(2026, 7, 3, 9, 20));
        order.setItems(List.of());
        order.setTotalAmount(BigDecimal.valueOf(120.00));
        order.setPaymentStatus(PaymentStatus.PENDING);

        PaymentClientResponse paymentResponse = new PaymentClientResponse();
        paymentResponse.setOrderId("14");
        paymentResponse.setStatus("FAILED");

        when(orderRepository.findById(14L)).thenReturn(Optional.of(order));
        when(paymentClient.processPayment(any())).thenReturn(paymentResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.checkoutOrder(14L, checkoutRequest(PaymentMethod.CREDIT_CARD));

        assertEquals(PaymentStatus.PAYMENT_FAILED, response.getPaymentStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void checkoutOrderAllowsRetryWhenOrderIsPaymentFailed() {
        Order order = new Order();
        order.setId(18L);
        order.setCustomerId("alice@example.com");
        order.setCreatedAt(LocalDateTime.of(2026, 7, 3, 10, 0));
        order.setItems(List.of());
        order.setTotalAmount(BigDecimal.valueOf(45.00));
        order.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);

        PaymentClientResponse paymentResponse = new PaymentClientResponse();
        paymentResponse.setOrderId("18");
        paymentResponse.setStatus("PAID");

        when(orderRepository.findById(18L)).thenReturn(Optional.of(order));
        when(paymentClient.processPayment(any())).thenReturn(paymentResponse);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.checkoutOrder(18L, checkoutRequest(PaymentMethod.PAYPAL));

        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void checkoutOrderThrowsConflictWhenOrderIsNotPendingOrPaymentFailed() {
        Order order = new Order();
        order.setId(15L);
        order.setCustomerId("alice@example.com");
        order.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findById(15L)).thenReturn(Optional.of(order));

        assertThrows(OrderCheckoutConflictException.class,
                () -> orderService.checkoutOrder(15L, checkoutRequest(PaymentMethod.CREDIT_CARD)));
        verify(paymentClient, never()).processPayment(any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void checkoutOrderThrowsNotFoundWhenOrderDoesNotExist() {
        when(orderRepository.findById(16L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.checkoutOrder(16L, checkoutRequest(PaymentMethod.CREDIT_CARD)));
        verify(paymentClient, never()).processPayment(any());
    }

    @Test
    void checkoutOrderThrowsServiceUnavailableWhenPaymentServiceCallFails() {
        Order order = new Order();
        order.setId(17L);
        order.setCustomerId("alice@example.com");
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(15.00));
        order.setItems(List.of());

        when(orderRepository.findById(17L)).thenReturn(Optional.of(order));
        when(paymentClient.processPayment(any())).thenThrow(mock(FeignException.class));

        assertThrows(PaymentServiceUnavailableException.class,
                () -> orderService.checkoutOrder(17L, checkoutRequest(PaymentMethod.CREDIT_CARD)));
        verify(orderRepository, never()).save(any(Order.class));
    }

    private CheckoutOrderRequest checkoutRequest(PaymentMethod paymentMethod) {
        CheckoutOrderRequest request = new CheckoutOrderRequest();
        request.setPaymentMethod(paymentMethod);
        return request;
    }
}
