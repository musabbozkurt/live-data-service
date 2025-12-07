package com.mb.livedataservice.integration_tests.repository;

import com.mb.livedataservice.data.model.OrderItem;
import com.mb.livedataservice.data.repository.OrderItemRepository;
import com.mb.livedataservice.data.repository.ScoreBoardRepository;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ScoreBoardRepository scoreBoardRepository;

    @Test
    void findByOrderId_shouldFindItemsForOrder() {
        List<OrderItem> results = orderItemRepository.findByOrderId(1L);

        assertThat(results)
                .isNotEmpty()
                .allMatch(item -> item.orderId().equals(1L));
    }

    @Test
    void findByOrderId_shouldReturnEmptyForNonExistentOrder() {
        List<OrderItem> results = orderItemRepository.findByOrderId(999L);

        assertThat(results).isEmpty();
    }

    @Test
    void findOrderItemsWithCoffeeDetails_shouldFindItemsOrderedByCoffeeName() {
        List<OrderItem> results = orderItemRepository.findOrderItemsWithCoffeeDetails(1L);

        assertThat(results)
                .isNotEmpty()
                .allMatch(item -> item.orderId().equals(1L));
    }

    @Test
    void findOrderItemsWithCoffeeDetails_shouldReturnEmptyForNonExistentOrder() {
        List<OrderItem> results = orderItemRepository.findOrderItemsWithCoffeeDetails(999L);

        assertThat(results).isEmpty();
    }
}
