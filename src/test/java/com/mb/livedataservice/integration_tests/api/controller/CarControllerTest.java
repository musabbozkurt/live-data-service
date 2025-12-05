package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.request.ApiCarRequest;
import com.mb.livedataservice.api.response.ApiCarResponse;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestcontainersConfiguration.class)
class CarControllerServerTest {

    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    @BeforeEach
    void setup() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void create_ShouldReturnCreatedCar_WhenValidInput() {
        // Arrange
        ApiCarRequest carRequest = new ApiCarRequest("Model S", 2023, "Tesla", List.of());

        // Act
        ApiCarResponse actualCar = restTestClient.post()
                .uri("/cars")
                .body(carRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiCarResponse.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualCar);
        assertNotNull(actualCar.id());
        assertEquals("Model S", actualCar.model());
        assertEquals(2023, actualCar.yearOfManufacture());
        assertEquals("Tesla", actualCar.brand());
    }

    @Test
    void getCarById_ShouldReturnCar_WhenCarExists() {
        // Arrange
        ApiCarRequest carRequest = new ApiCarRequest("Civic", 2022, "Honda", List.of());
        ApiCarResponse createdCar = restTestClient.post()
                .uri("/cars")
                .body(carRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiCarResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(createdCar);
        String carId = createdCar.id();

        // Act
        // Assertions
        restTestClient.get()
                .uri("/cars/{id}", carId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiCarResponse.class)
                .value(car -> {
                    assertNotNull(car);
                    assertEquals(carId, car.id());
                    assertEquals("Civic", car.model());
                    assertEquals(2022, car.yearOfManufacture());
                    assertEquals("Honda", car.brand());
                });
    }

    @Test
    void findAll_ShouldReturnPageOfCars_WhenCarsExist() {
        // Arrange
        ApiCarRequest carRequest = new ApiCarRequest("Corolla", 2021, "Toyota", List.of());
        restTestClient.post()
                .uri("/cars")
                .body(carRequest)
                .exchange()
                .expectStatus().isOk();

        // Act
        // Assertions
        restTestClient.get()
                .uri("/cars?page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete_ShouldDeleteCar_WhenCarExists() {
        // Arrange
        ApiCarRequest carRequest = new ApiCarRequest("Accord", 2020, "Honda", List.of());
        ApiCarResponse createdCar = restTestClient.post()
                .uri("/cars")
                .body(carRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiCarResponse.class)
                .returnResult()
                .getResponseBody();

        assertNotNull(createdCar);
        String carId = createdCar.id();

        // Act
        // Assertions
        restTestClient.delete()
                .uri("/cars/{id}", carId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void fuzzySearch_ShouldReturnMatchingCars_WhenFilterProvided() {
        // Arrange
        ApiCarRequest carRequest = new ApiCarRequest("Mustang", 2023, "Ford", List.of());
        restTestClient.post()
                .uri("/cars")
                .body(carRequest)
                .exchange()
                .expectStatus().isOk();

        // Act
        List<ApiCarResponse> cars = restTestClient.get()
                .uri("/cars/fuzzy-search?brand=Ford")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<ApiCarResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(cars);
    }

    @Test
    void server_ShouldBeRunning_WhenTestsExecute() {
        // Arrange
        // Server started via @SpringBootTest

        // Act
        // No explicit action needed

        // Assertions
        assertTrue(port > 0, "Server should be running on a port");
        assertNotEquals(8080, port, "Should be random port, not default");
    }
}
