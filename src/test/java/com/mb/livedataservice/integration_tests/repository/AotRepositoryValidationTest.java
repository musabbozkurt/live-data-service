package com.mb.livedataservice.integration_tests.repository;

import com.mb.livedataservice.data.repository.CoffeeRepository;
import com.mb.livedataservice.data.repository.OrderItemRepository;
import com.mb.livedataservice.data.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.CrudRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that Spring Data AOT successfully processes all custom repository methods.
 * Compares method signatures (not just names) to handle overloaded methods correctly.
 * Fails the build if any methods are skipped due to typos or invalid property references.
 */
class AotRepositoryValidationTest {

    private static final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void coffeeRepositoryMethodsAreAotProcessed() {
        validateRepository(CoffeeRepository.class, "com/mb/livedataservice/data/repository/CoffeeRepository.json");
    }

    @Test
    void orderRepositoryMethodsAreAotProcessed() {
        validateRepository(OrderRepository.class, "com/mb/livedataservice/data/repository/OrderRepository.json");
    }

    @Test
    void orderItemRepositoryMethodsAreAotProcessed() {
        validateRepository(OrderItemRepository.class, "com/mb/livedataservice/data/repository/OrderItemRepository.json");
    }

    private void validateRepository(Class<?> repositoryClass, String metadataPath) {
        JsonNode metadata = loadMetadata(metadataPath);
        Set<String> declaredSignatures = getCustomMethodSignatures(repositoryClass);
        Set<String> aotProcessedSignatures = extractMethodSignatures(metadata);

        Set<String> skippedSignatures = new HashSet<>(declaredSignatures);
        skippedSignatures.removeAll(aotProcessedSignatures);

        assertTrue(skippedSignatures.isEmpty(), "AOT skipped methods in %s: %s".formatted(repositoryClass.getSimpleName(), skippedSignatures));
    }

    private JsonNode loadMetadata(String relativePath) {
        Path path = Paths.get("target/classes", relativePath);
        assertTrue(Files.exists(path), "AOT metadata not found: " + path);
        return mapper.readTree(path.toFile());
    }

    private Set<String> extractMethodSignatures(JsonNode metadata) {
        return StreamSupport.stream(metadata.get("methods").spliterator(), false)
                .map(method -> {
                    String name = method.get("name").asString();
                    String signature = method.get("signature").asString();
                    return buildSignatureKey(name, signature);
                })
                .collect(Collectors.toSet());
    }

    private Set<String> getCustomMethodSignatures(Class<?> repositoryClass) {
        return Arrays.stream(repositoryClass.getDeclaredMethods())
                .filter(method -> !isInheritedCrudMethod(method))
                .map(this::buildSignatureKey)
                .collect(Collectors.toSet());
    }

    private String buildSignatureKey(Method method) {
        String paramTypes = Arrays.stream(method.getParameterTypes())
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));
        return "%s(%s)".formatted(method.getName(), paramTypes);
    }

    private String buildSignatureKey(String methodName, String fullSignature) {
        // Parse parameter types from full signature
        // Format: "public abstract ReturnType ClassName.methodName(ParamType1,ParamType2)"
        int paramsStart = fullSignature.indexOf('(');
        if (paramsStart == -1) {
            return "%s()".formatted(methodName);
        }

        int paramsEnd = fullSignature.indexOf(')', paramsStart);
        String params = fullSignature.substring(paramsStart + 1, paramsEnd);

        if (params.isEmpty()) {
            return "%s()".formatted(methodName);
        }

        // Extract simple class names from fully qualified parameter types
        String simplifiedParams = Arrays.stream(params.split(","))
                .map(String::trim)
                .map(this::extractSimpleClassName)
                .collect(Collectors.joining(","));

        return "%s(%s)".formatted(methodName, simplifiedParams);
    }

    private String extractSimpleClassName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot == -1 ? fullyQualifiedName : fullyQualifiedName.substring(lastDot + 1);
    }

    private boolean isInheritedCrudMethod(Method method) {
        try {
            CrudRepository.class.getMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException _) {
            return false;
        }
    }
}
