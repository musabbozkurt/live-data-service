package com.mb.livedataservice.service;

import com.mb.livedataservice.service.impl.ThymeleafTemplateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThymeleafTemplateServiceTest {

    private ThymeleafTemplateService thymeleafTemplateService;

    @BeforeEach
    void setUp() {
        thymeleafTemplateService = new ThymeleafTemplateServiceImpl();
    }

    @Test
    void processTemplate_ShouldReturnNull_WhenTemplateIsNull() {
        // Arrange
        Map<String, Object> variables = Map.of("name", "Test");

        // Act
        String result = thymeleafTemplateService.processTemplate(null, variables);

        // Assertions
        assertNull(result);
    }

    @Test
    void processTemplate_ShouldResolveSimpleVariable_WhenVariableProvided() {
        // Arrange
        String template = "<p th:text=\"${name}\">Default</p>";
        Map<String, Object> variables = Map.of("name", "John");

        // Act
        String result = thymeleafTemplateService.processTemplate(template, variables);

        // Assertions
        assertTrue(result.contains("John"));
        assertFalse(result.contains("Default"));
    }

    @Test
    void processTemplate_ShouldIterateOverList_WhenListProvided() {
        // Arrange
        String template = """
                <table>
                    <tr th:each="item : ${items}">
                        <td th:text="${item.name}">Name</td>
                        <td th:text="${item.value}">Value</td>
                    </tr>
                </table>
                """;

        List<Map<String, String>> items = List.of(
                Map.of("name", "Item 1", "value", "100"),
                Map.of("name", "Item 2", "value", "200"),
                Map.of("name", "Item 3", "value", "300")
        );

        Map<String, Object> variables = Map.of("items", items);

        // Act
        String result = thymeleafTemplateService.processTemplate(template, variables);

        // Assertions
        assertTrue(result.contains("Item 1"));
        assertTrue(result.contains("Item 2"));
        assertTrue(result.contains("Item 3"));
        assertTrue(result.contains("100"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("300"));
    }

    @Test
    void processTemplate_ShouldHandleAlternatingStyles_WhenIterStatUsed() {
        // Arrange
        String template = """
                <table>
                    <tr th:each="item, iterStat : ${items}"
                        th:style="${iterStat.odd} ? 'background-color: #ffffff;' : 'background-color: #fafafa;'">
                        <td th:text="${item.name}">Name</td>
                    </tr>
                </table>
                """;

        List<Map<String, String>> items = List.of(
                Map.of("name", "Row 1"),
                Map.of("name", "Row 2"),
                Map.of("name", "Row 3")
        );

        Map<String, Object> variables = Map.of("items", items);

        // Act
        String result = thymeleafTemplateService.processTemplate(template, variables);

        // Assertions
        assertTrue(result.contains("Row 1"));
        assertTrue(result.contains("Row 2"));
        assertTrue(result.contains("Row 3"));
        assertTrue(result.contains("background-color: #ffffff"));
        assertTrue(result.contains("background-color: #fafafa"));
    }

    @Test
    void processTemplate_ShouldHandleApiDocumentationTemplate_WhenAllVariablesProvided() {
        // Arrange
        String template = """
                <div>
                    <strong th:text="${totalApiCount}">0</strong> API endpoints
                    <table>
                        <tr th:each="endpoint : ${endpoints}">
                            <td th:text="${endpoint.method}">Method</td>
                            <td th:text="${endpoint.path}">Path</td>
                            <td th:text="${endpoint.description}">Description</td>
                        </tr>
                    </table>
                    <p>Base URL: <span th:text="${baseUrl}">url</span></p>
                    <a th:href="${swaggerUrl}">View Docs</a>
                </div>
                """;

        List<Map<String, Object>> endpoints = List.of(
                Map.of("method", "GET", "path", "/api/v1/users", "description", "Get all users"),
                Map.of("method", "POST", "path", "/api/v1/users", "description", "Create user")
        );

        Map<String, Object> variables = new HashMap<>();
        variables.put("totalApiCount", "2");
        variables.put("endpoints", endpoints);
        variables.put("baseUrl", "https://api.mb.test/v1");
        variables.put("swaggerUrl", "https://api.mb.test/swagger-ui.html");

        // Act
        String result = thymeleafTemplateService.processTemplate(template, variables);

        // Assertions
        assertTrue(result.contains(">2</strong>"));
        assertTrue(result.contains("GET"));
        assertTrue(result.contains("POST"));
        assertTrue(result.contains("/api/v1/users"));
        assertTrue(result.contains("Get all users"));
        assertTrue(result.contains("Create user"));
        assertTrue(result.contains("https://api.mb.test/v1"));
        assertTrue(result.contains("https://api.mb.test/swagger-ui.html"));
    }

    @Test
    void processTemplate_ShouldHandleEmptyVariables_WhenNoVariablesProvided() {
        // Arrange
        String template = "<p>Static content</p>";

        // Act
        String result = thymeleafTemplateService.processTemplate(template, null);

        // Assertions
        assertTrue(result.contains("Static content"));
    }

    @Test
    void processTemplate_ShouldHandleEmptyList_WhenEmptyListProvided() {
        // Arrange
        String template = """
                <table>
                    <tr th:each="item : ${items}">
                        <td th:text="${item.name}">Name</td>
                    </tr>
                </table>
                """;

        Map<String, Object> variables = Map.of("items", List.of());

        // Act
        String result = thymeleafTemplateService.processTemplate(template, variables);

        // Assertions
        assertNotNull(result);
        assertFalse(result.contains("<td>"));
    }

    @Test
    void processTemplate_ShouldHandleManyRows_WhenLargeListProvided() {
        // Arrange
        String template = """
                <table>
                    <tr th:each="item, iterStat : ${items}"
                        th:style="${iterStat.odd} ? 'background-color: #ffffff;' : 'background-color: #fafafa;'">
                        <td th:text="${item.name}">Name</td>
                    </tr>
                </table>
                """;

        List<Map<String, String>> items = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            items.add(Map.of("name", "Row " + (i + 1)));
        }

        Map<String, Object> variables = Map.of("items", items);

        // Act
        String result = thymeleafTemplateService.processTemplate(template, variables);

        // Assertions
        for (int i = 0; i < 10; i++) {
            assertTrue(result.contains("Row " + (i + 1)));
        }
        // Check alternating rows
        assertTrue(result.contains("background-color: #ffffff"));
        assertTrue(result.contains("background-color: #fafafa"));
    }
}
