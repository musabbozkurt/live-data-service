package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.client.jsonplaceholder.TodoService;
import com.mb.livedataservice.client.jsonplaceholder.request.Todo;
import com.mb.livedataservice.integration_tests.config.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = TestcontainersConfiguration.class)
class TodoControllerContextTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TodoService todoService;

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
        restTestClient = RestTestClient.bindToApplicationContext(webApplicationContext).build();
    }

    @Test
    void findAll_ShouldReturnListOfTodos_WhenTodosExist() {
        // Arrange
        // No explicit arrangement needed - uses real TodoService

        // Act
        List<Todo> actualTodos = restTestClient.get()
                .uri("/api/todos")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Todo>>() {
                })
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodos);
        assertEquals(200, actualTodos.size());
        assertEquals("delectus aut autem", actualTodos.getFirst().title());
        assertFalse(actualTodos.getFirst().completed());
    }

    @Test
    void findById_ShouldReturnTodo_WhenTodoExists() {
        // Arrange
        int todoId = 1;

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
        assertEquals(1, actualTodo.id());
        assertEquals("delectus aut autem", actualTodo.title());
        assertFalse(actualTodo.completed());
        assertEquals(1, actualTodo.userId());
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
        Todo inputTodo = new Todo(null, 1, "New Integration Test Todo", false);

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
        assertEquals("New Integration Test Todo", actualTodo.title());
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
        Todo updateTodo = new Todo(todoId, 1, "Updated Integration Test Todo", true);

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
        assertEquals("Updated Integration Test Todo", actualTodo.title());
        assertTrue(actualTodo.completed());
    }

    @Test
    void update_ShouldReturnUpdatedTodo_WhenPartialUpdate() {
        // Arrange
        int todoId = 2;
        Todo updateTodo = new Todo(todoId, 1, "Partially Updated Todo", false);

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
        assertEquals("Partially Updated Todo", actualTodo.title());
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
    void context_ShouldLoadTodoService_WhenApplicationContextLoads() {
        // Arrange
        // Context already loaded via @Autowired

        // Act
        // No explicit action needed

        // Assertions
        assertNotNull(todoService);
        assertNotNull(webApplicationContext);
    }
}
