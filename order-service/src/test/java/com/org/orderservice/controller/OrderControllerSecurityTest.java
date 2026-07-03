package com.org.orderservice.controller;

import com.org.orderservice.config.SecurityConfig;
import com.org.orderservice.dto.OrderResponse;
import com.org.orderservice.security.JwtAuthenticationFilter;
import com.org.orderservice.security.JwtService;
import com.org.orderservice.service.OrderService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = "jwt.secret=super-secret-key")
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void getOrdersReturnsUnauthorizedWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrderReturnsUnauthorizedWhenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[]}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrdersAllowsValidJwtAndUsesPrincipalSubject() throws Exception {
        when(orderService.getAllOrders(eq("alice@example.com"))).thenReturn(Collections.<OrderResponse>emptyList());

        mockMvc.perform(get("/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createToken("alice@example.com")))
                .andExpect(status().isOk());

        verify(orderService).getAllOrders("alice@example.com");
    }

    @Test
    void getOrderAllowsValidJwtAndUsesPrincipalSubject() throws Exception {
        OrderResponse response = new OrderResponse();
        response.setId(42L);
        response.setCustomerId("alice@example.com");
        when(orderService.getOrderById(anyLong(), eq("alice@example.com"))).thenReturn(response);

        mockMvc.perform(get("/orders/42")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + createToken("alice@example.com")))
                .andExpect(status().isOk());

        verify(orderService).getOrderById(42L, "alice@example.com");
    }

    private String createToken(String subject) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 60000))
                .signWith(SignatureAlgorithm.HS256, "super-secret-key".getBytes())
                .compact();
    }
}
