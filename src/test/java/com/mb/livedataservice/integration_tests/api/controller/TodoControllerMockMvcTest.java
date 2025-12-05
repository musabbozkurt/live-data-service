package com.mb.livedataservice.integration_tests.api.controller;

import com.mb.livedataservice.api.controller.TodoController;
import com.mb.livedataservice.client.jsonplaceholder.TodoService;
import com.mb.livedataservice.client.jsonplaceholder.request.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = TodoController.class)
@ContextConfiguration(classes = {TodoController.class})
class TodoControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
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
        restTestClient = RestTestClient.bindTo(mockMvc).build();
    }

    @Test
    void findAll_ShouldReturnListOfTodos_WhenTodosExist() {
        // Arrange
        List<Todo> expectedTodos = List.of(
                new Todo(1, 1, "First Todo", true),
                new Todo(2, 1, "Second Todo", false)
        );
        when(todoService.findAll()).thenReturn(expectedTodos);

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
        assertEquals(2, actualTodos.size());
        assertEquals("First Todo", actualTodos.get(0).title());
        assertEquals("Second Todo", actualTodos.get(1).title());
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoTodosExist() {
        // Arrange
        when(todoService.findAll()).thenReturn(Collections.emptyList());

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
        assertEquals(0, actualTodos.size());
    }

    @Test
    void findById_ShouldReturnTodo_WhenTodoExists() {
        // Arrange
        Todo expectedTodo = new Todo(1, 1, "Test Todo", true);
        when(todoService.findById(1)).thenReturn(expectedTodo);

        // Act
        Todo actualTodo = restTestClient.get()
                .uri("/api/todos/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodo);
        assertEquals(1, actualTodo.id());
        assertEquals("Test Todo", actualTodo.title());
        assertTrue(actualTodo.completed());
    }

    @Test
    void create_ShouldReturnCreatedTodo_WhenValidInput() {
        // Arrange
        Todo inputTodo = new Todo(null, 1, "New Todo", false);
        Todo createdTodo = new Todo(1, 1, "New Todo", false);
        when(todoService.create(any(Todo.class))).thenReturn(createdTodo);

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
        assertEquals(1, actualTodo.id());
        assertEquals("New Todo", actualTodo.title());
        verify(todoService).create(any(Todo.class));
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
        Todo inputTodo = new Todo(1, 1, "Updated Todo", true);
        Todo updatedTodo = new Todo(1, 1, "Updated Todo", true);
        when(todoService.update(eq(1), any(Todo.class))).thenReturn(updatedTodo);

        // Act
        Todo actualTodo = restTestClient.put()
                .uri("/api/todos/1")
                .body(inputTodo)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Todo.class)
                .returnResult()
                .getResponseBody();

        // Assertions
        assertNotNull(actualTodo);
        assertEquals(1, actualTodo.id());
        assertEquals("Updated Todo", actualTodo.title());
        assertTrue(actualTodo.completed());
        verify(todoService).update(eq(1), any(Todo.class));
    }

    @Test
    void delete_ShouldReturnNoContent_WhenTodoExists() {
        // Arrange
        doNothing().when(todoService).delete(1);

        // Act
        // Assertions
        restTestClient.delete()
                .uri("/api/todos/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(todoService).delete(1);
    }
}
