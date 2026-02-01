package com.mb.livedataservice.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemplateUtilsTest {

    @Test
    void resolvePlaceholders_ShouldReplacePlaceholder_WhenSinglePlaceholderExists() {
        // Arrange
        String template = "Hello {{name}}, welcome!";
        Map<String, String> parameters = Map.of("name", "John");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Hello John, welcome!", result);
    }

    @Test
    void resolvePlaceholders_ShouldReplaceAllPlaceholders_WhenMultiplePlaceholdersExist() {
        // Arrange
        String template = "Dear {{firstName}} {{lastName}}, your order {{orderId}} is ready.";
        Map<String, String> parameters = Map.of(
                "firstName", "John",
                "lastName", "Doe",
                "orderId", "12345"
        );

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Dear John Doe, your order 12345 is ready.", result);
    }

    @Test
    void resolvePlaceholders_ShouldReplaceAllOccurrences_WhenSamePlaceholderAppearsMultipleTimes() {
        // Arrange
        String template = "{{name}} said hello. {{name}} is happy.";
        Map<String, String> parameters = Map.of("name", "John");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("John said hello. John is happy.", result);
    }

    @Test
    void resolvePlaceholders_ShouldReplaceWithEmptyString_WhenValueIsNull() {
        // Arrange
        String template = "Hello {{name}}, welcome!";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("name", null);

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Hello , welcome!", result);
    }

    @Test
    void resolvePlaceholders_ShouldReturnOriginalText_WhenNoMatchingPlaceholdersExist() {
        // Arrange
        String template = "Hello {{name}}, welcome!";
        Map<String, String> parameters = Map.of("different", "value");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Hello {{name}}, welcome!", result);
    }

    @Test
    void resolvePlaceholders_ShouldReturnNull_WhenTextIsNull() {
        // Arrange
        Map<String, String> parameters = Map.of("name", "John");

        // Act
        // Assertions
        assertNull(TemplateUtils.resolvePlaceholders(null, parameters));
    }

    @Test
    void resolvePlaceholders_ShouldReturnOriginalText_WhenParametersIsNull() {
        // Arrange
        String template = "Hello {{name}}, welcome!";

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, null);

        // Assertions
        assertEquals("Hello {{name}}, welcome!", result);
    }

    @Test
    void resolvePlaceholders_ShouldReturnOriginalText_WhenParametersIsEmpty() {
        // Arrange
        String template = "Hello {{name}}, welcome!";
        Map<String, String> parameters = Map.of();

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Hello {{name}}, welcome!", result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void resolvePlaceholders_ShouldReturnSameValue_WhenTextIsNullOrEmpty(String text) {
        // Arrange
        Map<String, String> parameters = Map.of("name", "John");

        // Act
        String result = TemplateUtils.resolvePlaceholders(text, parameters);

        // Assertions
        assertEquals(text, result);
    }

    @Test
    void resolvePlaceholders_ShouldReturnOriginalText_WhenNoPlaceholdersInText() {
        // Arrange
        String template = "Hello world, welcome!";
        Map<String, String> parameters = Map.of("name", "John");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Hello world, welcome!", result);
    }

    @Test
    void resolvePlaceholders_ShouldHandleSpecialCharacters_WhenValuesContainSpecialCharacters() {
        // Arrange
        String template = "Message: {{content}}";
        Map<String, String> parameters = Map.of("content", "Hello <script>alert('test')</script>");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Message: Hello <script>alert('test')</script>", result);
    }

    @Test
    void resolvePlaceholders_ShouldHandleUnicode_WhenValuesContainUnicodeCharacters() {
        // Arrange
        String template = "Hello {{name}}, welcome to MB Test!";
        Map<String, String> parameters = Map.of("name", "José García");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("Hello José García, welcome to MB Test!", result);
    }

    @Test
    void resolvePlaceholders_ShouldResolveApiDocumentationTemplate_WhenAllPlaceholdersProvided() {
        // Arrange
        String template = """
                <strong>{{TOTAL_API_COUNT}}</strong> API endpoints available
                <td>{{METHOD_1}}</td>
                <td>{{ENDPOINT_1}}</td>
                <td>{{DESCRIPTION_1}}</td>
                <td>{{AUTH_REQUIRED_1}}</td>
                <p>{{BASE_URL}}</p>
                <p>{{API_VERSION}}</p>
                <a href="{{SWAGGER_URL}}">View Documentation</a>
                """;

        Map<String, String> parameters = new HashMap<>();
        parameters.put("TOTAL_API_COUNT", "8");
        parameters.put("METHOD_1", "GET");
        parameters.put("ENDPOINT_1", "/api/v1/users");
        parameters.put("DESCRIPTION_1", "Get all users");
        parameters.put("AUTH_REQUIRED_1", "Yes");
        parameters.put("BASE_URL", "https://api.mb.test/v1");
        parameters.put("API_VERSION", "v1.0");
        parameters.put("SWAGGER_URL", "https://api.mb.test/swagger-ui.html");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertTrue(result.contains("<strong>8</strong> API endpoints available"));
        assertTrue(result.contains("<td>GET</td>"));
        assertTrue(result.contains("<td>/api/v1/users</td>"));
        assertTrue(result.contains("<td>Get all users</td>"));
        assertTrue(result.contains("<td>Yes</td>"));
        assertTrue(result.contains("<p>https://api.mb.test/v1</p>"));
        assertTrue(result.contains("<p>v1.0</p>"));
        assertTrue(result.contains("href=\"https://api.mb.test/swagger-ui.html\""));
        assertFalse(result.contains("{{"));
        assertFalse(result.contains("}}"));
    }

    @Test
    void resolvePlaceholders_ShouldResolveMultipleApiEndpoints_WhenMultipleEndpointPlaceholdersProvided() {
        // Arrange
        String template = """
                <!-- API Endpoint 1 -->
                <td>{{METHOD_1}}</td><td>{{ENDPOINT_1}}</td>
                <!-- API Endpoint 2 -->
                <td>{{METHOD_2}}</td><td>{{ENDPOINT_2}}</td>
                <!-- API Endpoint 3 -->
                <td>{{METHOD_3}}</td><td>{{ENDPOINT_3}}</td>
                """;

        Map<String, String> parameters = new HashMap<>();
        parameters.put("METHOD_1", "GET");
        parameters.put("ENDPOINT_1", "/api/v1/users");
        parameters.put("METHOD_2", "POST");
        parameters.put("ENDPOINT_2", "/api/v1/users");
        parameters.put("METHOD_3", "DELETE");
        parameters.put("ENDPOINT_3", "/api/v1/users/{id}");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertTrue(result.contains("<td>GET</td><td>/api/v1/users</td>"));
        assertTrue(result.contains("<td>POST</td><td>/api/v1/users</td>"));
        assertTrue(result.contains("<td>DELETE</td><td>/api/v1/users/{id}</td>"));
        assertFalse(result.contains("{{"));
        assertFalse(result.contains("}}"));
    }

    @Test
    void resolvePlaceholders_ShouldHandleComplexHtmlTemplate_WhenTemplateContainsInlineStyles() {
        // Arrange
        String template = """
                <td style="padding: 16px 12px; font-size: 14px; color: #FF6B00; font-weight: 700;">
                    {{ENDPOINT}}
                </td>
                <span style="display: inline-block; padding: 4px 10px; background-color: #d4edda;">
                    {{METHOD}}
                </span>
                """;

        Map<String, String> parameters = Map.of(
                "ENDPOINT", "/api/v1/orders",
                "METHOD", "GET"
        );

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertTrue(result.contains("/api/v1/orders"));
        assertTrue(result.contains("GET"));
        assertTrue(result.contains("style=\"padding: 16px 12px;"));
        assertFalse(result.contains("{{"));
        assertFalse(result.contains("}}"));
    }

    @Test
    void resolvePlaceholders_ShouldKeepUnmatchedPlaceholders_WhenSomePlaceholdersNotProvided() {
        // Arrange
        String template = """
                <strong>{{TOTAL_API_COUNT}}</strong> API endpoints
                <td>{{METHOD_1}}</td>
                <td>{{METHOD_2}}</td>
                """;

        Map<String, String> parameters = Map.of(
                "TOTAL_API_COUNT", "2",
                "METHOD_1", "GET"
        );

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertTrue(result.contains("<strong>2</strong> API endpoints"));
        assertTrue(result.contains("<td>GET</td>"));
        assertTrue(result.contains("{{METHOD_2}}"));
    }

    @Test
    void resolvePlaceholders_ShouldHandleCurrencyFormat_WhenValueContainsCurrency() {
        // Arrange
        String template = "<p>Total Amount: {{TOTAL_AMOUNT}}</p>";
        Map<String, String> parameters = Map.of("TOTAL_AMOUNT", "$1,234,567.89");

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("<p>Total Amount: $1,234,567.89</p>", result);
    }

    @Test
    void resolvePlaceholders_ShouldHandleUrlPlaceholder_WhenValueContainsFullUrl() {
        // Arrange
        String template = "<a href=\"{{API_DOC_URL}}\">View Docs</a>";
        Map<String, String> parameters = Map.of(
                "API_DOC_URL", "https://api.mb.test/swagger-ui.html?endpoint=users&action=list"
        );

        // Act
        String result = TemplateUtils.resolvePlaceholders(template, parameters);

        // Assertions
        assertEquals("<a href=\"https://api.mb.test/swagger-ui.html?endpoint=users&action=list\">View Docs</a>", result);
    }
}
