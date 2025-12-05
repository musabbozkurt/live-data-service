package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.client.jsonplaceholder.request.Todo;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TestcontainersConfiguration.class)
class TodoControllerServerTest {

    @LocalServerPort
    private int port;

    private RestTestClient restTestClient;

    private static Stream<Arguments> invalidTitleProvider() {
        return Stream.of(
                Arguments.of(""),
                Arguments.of((String) null),
                Arguments.of("   ")
        );
    }

    @BeforeEach
    void setup() {
        restTestClient = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    void findAll_ShouldReturnListOfTodos_WhenTodosExist() {
        // Arrange
        // No explicit arrangement needed - uses real server

        // Act
        List<Todo> todos = restTestClient.get()
                .uri("/api/todos")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<@NonNull List<Todo>>() {
                })
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(todos);
        assertEquals(200, todos.size());
        assertEquals("delectus aut autem", todos.getFirst().title());
        assertFalse(todos.getFirst().completed());
    }

    @Test
    void findById_ShouldReturnTodo_WhenTodoExists() {
        // Arrange
        int todoId = 1;

        // Act
        // Assertions
        restTestClient.get()
                .uri("/api/todos/{id}", todoId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .value(todo -> {
                    assertNotNull(todo);
                    assertEquals(1, todo.id());
                    assertEquals("delectus aut autem", todo.title());
                    assertFalse(todo.completed());
                    assertEquals(1, todo.userId());
                });
    }

    @Test
    void findById_ShouldReturnTodo_WhenDifferentTodoRequested() {
        // Arrange
        int todoId = 5;

        // Act
        Todo actualTodo = restTestClient.get()
                .uri("/api/todos/{id}", todoId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodo);
        assertEquals(5, actualTodo.id());
        assertNotNull(actualTodo.title());
        assertEquals(1, actualTodo.userId());
    }

    @Test
    void create_ShouldReturnCreatedTodo_WhenValidInput() {
        // Arrange
        Todo inputTodo = new Todo(null, 1, "Server Test Todo", false);

        // Act
        Todo actualTodo = restTestClient.post()
                .uri("/api/todos")
                .body(inputTodo)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodo);
        assertNotNull(actualTodo.id());
        assertEquals("Server Test Todo", actualTodo.title());
        assertFalse(actualTodo.completed());
        assertEquals(1, actualTodo.userId());
    }

    @ParameterizedTest
    @MethodSource("invalidTitleProvider")
    void create_ShouldReturnBadRequest_WhenTitleIsInvalid(String title) {
        // Arrange
        Todo invalidTodo = new Todo(null, 1, title, false);

        // Act
        // Assertions
        restTestClient.post()
                .uri("/api/todos")
                .body(invalidTodo)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void update_ShouldReturnUpdatedTodo_WhenValidInput() {
        // Arrange
        int todoId = 1;
        Todo updateTodo = new Todo(todoId, 1, "Server Updated Todo", true);

        // Act
        Todo actualTodo = restTestClient.put()
                .uri("/api/todos/{id}", todoId)
                .body(updateTodo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodo);
        assertEquals(todoId, actualTodo.id());
        assertEquals("Server Updated Todo", actualTodo.title());
        assertTrue(actualTodo.completed());
    }

    @Test
    void update_ShouldReturnUpdatedTodo_WhenCompletedStatusChanges() {
        // Arrange
        int todoId = 3;
        Todo updateTodo = new Todo(todoId, 1, "Status Change Todo", false);

        // Act
        Todo actualTodo = restTestClient.put()
                .uri("/api/todos/{id}", todoId)
                .body(updateTodo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodo);
        assertEquals(todoId, actualTodo.id());
        assertFalse(actualTodo.completed());
    }

    @Test
    void delete_ShouldReturnNoContent_WhenTodoExists() {
        // Arrange
        int todoId = 1;

        // Act
        // Assertions
        restTestClient.delete()
                .uri("/api/todos/{id}", todoId)
                .exchange()
                .expectStatus().isNoContent();
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
