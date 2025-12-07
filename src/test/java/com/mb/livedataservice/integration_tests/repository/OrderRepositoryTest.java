package com.mb.livedataservice.integration_tests.repository;

import com.mb.livedataservice.data.model.Order;
import com.mb.livedataservice.data.repository.OrderRepository;
import com.mb.livedataservice.data.repository.ScoreBoardRepository;
import com.mb.livedataservice.enums.OrderStatus;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private ScoreBoardRepository scoreBoardRepository;

    @Test
    void findByCustomerName_shouldFindOrdersForCustomer() {
        List<Order> results = orderRepository.findByCustomerName("Alice Johnson");

        assertThat(results)
                .isNotEmpty()
                .allMatch(order -> order.customerName().equals("Alice Johnson"));
    }

    @Test
    void findByCustomerName_shouldReturnEmptyForUnknownCustomer() {
        List<Order> results = orderRepository.findByCustomerName("Unknown Customer");

        assertThat(results).isEmpty();
    }

    @Test
    void findByStatusAndOrderDateAfter_shouldFilterByStatusAndDate() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<Order> results = orderRepository.findByStatusAndOrderDateAfter(OrderStatus.PENDING, oneWeekAgo);

        assertThat(results)
                .isNotEmpty()
                .allMatch(order -> order.status() == OrderStatus.PENDING && order.orderDate().isAfter(oneWeekAgo));
    }

    @Test
    void findByStatusAndOrderDateAfter_shouldReturnEmptyForFutureDate() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);

        List<Order> results = orderRepository.findByStatusAndOrderDateAfter(OrderStatus.PENDING, futureDate);

        assertThat(results).isEmpty();
    }

    @Test
    void findOrdersByCoffeeName_shouldFindOrdersContainingCoffee() {
        List<Order> results = orderRepository.findOrdersByCoffeeName("Cappuccino");

        assertThat(results).isNotEmpty();
    }

    @Test
    void findOrdersByCoffeeName_shouldReturnEmptyForNonExistentCoffee() {
        List<Order> results = orderRepository.findOrdersByCoffeeName("Unicorn Frappuccino");

        assertThat(results).isEmpty();
    }
}
