package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.controller.OrderController;
import com.mb.livedataservice.data.model.Order;
import com.mb.livedataservice.data.model.OrderItem;
import com.mb.livedataservice.data.repository.OrderItemRepository;
import com.mb.livedataservice.data.repository.OrderRepository;
import com.mb.livedataservice.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Controller tests using RestTestClient - Spring Framework 7's unified REST testing API.
 * <p>
 * RestTestClient provides a consistent API across different testing scenarios:
 * - bindToController() - Unit test without Spring context (fastest)
 * - bindTo(mockMvc) - MVC slice test with validation/security (used here)
 * - bindToApplicationContext() - Full integration test
 * - bindToServer() - Real HTTP server test
 * <p>
 * See CoffeeControllerTest for an alternative approach using MockMvcTester with AssertJ.
 */
@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = {OrderController.class})
class OrderControllerTest {

    private RestTestClient restTestClient;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void shouldReturnAllOrders() {
        var orders = List.of(
                new Order(1L, 1L, "Alice", LocalDateTime.now(), new BigDecimal("10.00"), OrderStatus.PENDING)
        );
        when(orderRepository.findAll()).thenReturn(orders);

        restTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void shouldReturnOrdersByCustomer() {
        var orders = List.of(
                new Order(1L, 1L, "Alice", LocalDateTime.now(), new BigDecimal("10.00"), OrderStatus.DELIVERED)
        );
        when(orderRepository.findByCustomerName("Alice")).thenReturn(orders);

        restTestClient.get().uri("/api/orders/customer/Alice")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerName").isEqualTo("Alice");
    }

    @Test
    void shouldReturnOrdersByCoffee() {
        var orders = List.of(
                new Order(1L, 1L, "Bob", LocalDateTime.now(), new BigDecimal("5.00"), OrderStatus.READY)
        );
        when(orderRepository.findOrdersByCoffeeName("Latte")).thenReturn(orders);

        restTestClient.get().uri("/api/orders/by-coffee?coffeeName=Latte")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1);
    }

    @Test
    void shouldReturnOrderItems() {
        var items = List.of(
                new OrderItem(1L, 1L, 1L, 2, new BigDecimal("4.50"))
        );
        when(orderItemRepository.findOrderItemsWithCoffeeDetails(1L)).thenReturn(items);

        restTestClient.get().uri("/api/orders/1/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].quantity").isEqualTo(2);
    }
}
