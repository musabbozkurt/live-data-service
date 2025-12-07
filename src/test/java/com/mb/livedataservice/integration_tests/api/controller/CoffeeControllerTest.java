package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.controller.CoffeeController;
import com.mb.livedataservice.data.model.Coffee;
import com.mb.livedataservice.data.repository.CoffeeRepository;
import com.mb.livedataservice.enums.Size;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(CoffeeController.class)
@ContextConfiguration(classes = {CoffeeController.class})
class CoffeeControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private CoffeeRepository coffeeRepository;

    @Test
    void shouldReturnAllCoffees() {
        var coffees = List.of(
                new Coffee(1L, "Espresso", "Rich espresso", new BigDecimal("2.50"), Size.SMALL),
                new Coffee(2L, "Latte", "Creamy latte", new BigDecimal("4.50"), Size.MEDIUM)
        );
        when(coffeeRepository.findAll()).thenReturn(coffees);

        assertThat(mockMvcTester.get().uri("/api/coffee"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asArray()
                .hasSize(2);
    }

    @Test
    void shouldSearchCoffeesByName() {
        var coffees = List.of(
                new Coffee(1L, "Vanilla Latte", "Vanilla flavored", new BigDecimal("5.00"), Size.LARGE)
        );
        when(coffeeRepository.findByNameContainingIgnoreCase("latte")).thenReturn(coffees);

        assertThat(mockMvcTester.get().uri("/api/coffee/search?pattern=latte"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("Vanilla Latte");
    }

    @Test
    void shouldFilterCoffeesBySizeAndPrice() {
        var coffees = List.of(
                new Coffee(1L, "Large Mocha", "Chocolate mocha", new BigDecimal("6.25"), Size.LARGE)
        );
        when(coffeeRepository.findBySizeAndPriceGreaterThan(Size.LARGE, new BigDecimal("5.00"))).thenReturn(coffees);

        assertThat(mockMvcTester.get().uri("/api/coffee/filter?size=LARGE&minPrice=5.00"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].size")
                .isEqualTo("LARGE");
    }

    @Test
    void shouldReturnAffordableCoffees() {
        var coffees = List.of(
                new Coffee(1L, "Cold Brew", "Smooth cold brew", new BigDecimal("5.00"), Size.LARGE)
        );
        when(coffeeRepository.findAffordableCoffeesBySize("LARGE", new BigDecimal("6.00"))).thenReturn(coffees);

        assertThat(mockMvcTester.get().uri("/api/coffee/affordable?size=LARGE&maxPrice=6.00"))
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$")
                .asArray()
                .isNotEmpty();
    }
}
