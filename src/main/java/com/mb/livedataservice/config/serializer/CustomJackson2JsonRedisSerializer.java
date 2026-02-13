package com.mb.livedataservice.config.serializer;

import com.mb.livedataservice.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom Redis serializer that properly handles polymorphic types with @class property.
 * This serializer can deserialize arrays with @class inside each object without requiring
 * the array wrapper format (e.g., ["java.util.ArrayList", [...]]).
 * <p>
 * Supports legacy format: [{"@class":"com.example.Dto",...}]
 * And new format: ["java.util.ArrayList",[{"@class":"com.example.Dto",...}]]
 * <p>
 * See: <a href="https://github.com/spring-projects/spring-data-redis/issues/2361">spring-data-redis#2361</a>
 */
@Slf4j
public class CustomJackson2JsonRedisSerializer implements RedisSerializer<Object> {

    private static final String CLASS_PROPERTY = "@class";
    private final ObjectMapper objectMapper;

    public CustomJackson2JsonRedisSerializer() {
        this.objectMapper = JsonUtils.createMapper();
    }

    @Override
    public byte @NonNull [] serialize(@Nullable Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }

        return switch (value) {
            case Collection<?> collection -> serializeCollection(collection);
            case Map<?, ?> map -> serializeMap(map);
            default -> serializeObject(value);
        };
    }

    @Override
    @Nullable
    public Object deserialize(byte @Nullable [] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            String json = new String(bytes, StandardCharsets.UTF_8).trim();

            // Handle array format: [{"@class":"...", ...}, ...]
            if (json.startsWith("[")) {
                return deserializeArray(bytes);
            }

            // Handle single object format
            if (json.startsWith("{")) {
                return deserializeObject(bytes);
            }

            // For primitive types or other formats, return as-is
            return objectMapper.readValue(bytes, Object.class);
        } catch (SerializationException se) {
            log.error("SerializationException during deserialization. Exception: {}", ExceptionUtils.getStackTrace(se));
            throw se;
        } catch (Exception e) {
            log.error("Error occurred during deserialization. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new SerializationException("Could not read JSON: " + e.getMessage());
        }
    }

    private byte[] serializeCollection(@NonNull Collection<?> collection) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Object item : collection) {
            if (item != null) {
                ObjectNode objectNode = objectMapper.valueToTree(item);
                // Add @class as the first property
                ObjectNode newNode = objectMapper.createObjectNode();
                newNode.put(CLASS_PROPERTY, item.getClass().getName());

                if (objectNode != null && objectNode.properties() != null) {
                    objectNode.properties().forEach(entry -> newNode.set(entry.getKey(), entry.getValue()));
                }

                arrayNode.add(newNode);
            }
        }
        return objectMapper.writeValueAsBytes(arrayNode);
    }

    private byte[] serializeMap(@NonNull Map<?, ?> map) {
        // Serialize map without @class in the map itself - just serialize the entries
        // The map values will have @class if they are complex objects
        ObjectNode mapNode = objectMapper.createObjectNode();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value != null) {
                JsonNode valueNode = objectMapper.valueToTree(value);
                mapNode.set(key, valueNode);
            }
        }
        return objectMapper.writeValueAsBytes(mapNode);
    }

    private byte[] serializeObject(@NonNull Object value) {
        ObjectNode objectNode = objectMapper.valueToTree(value);
        ObjectNode newNode = objectMapper.createObjectNode();
        newNode.put(CLASS_PROPERTY, value.getClass().getName());

        if (objectNode != null && objectNode.properties() != null) {
            objectNode.properties().forEach(entry -> newNode.set(entry.getKey(), entry.getValue()));
        }

        return objectMapper.writeValueAsBytes(newNode);
    }

    private Object deserializeArray(byte[] bytes) {
        JsonNode rootNode = objectMapper.readTree(bytes);

        if (!rootNode.isArray()) {
            throw new SerializationException("Expected array but got: " + rootNode.getNodeType());
        }

        ArrayNode arrayNode = (ArrayNode) rootNode;

        if (arrayNode.isEmpty()) {
            return new ArrayList<>();
        }

        // Check if it's the new wrapper format: ["java.util.ArrayList", [...]]
        if (arrayNode.size() == 2 && arrayNode.get(0).isString() && arrayNode.get(1).isArray()) {
            String collectionType = arrayNode.get(0).asString();
            try {
                Class<?> collectionClass = Class.forName(collectionType);
                if (Collection.class.isAssignableFrom(collectionClass)) {
                    ArrayNode elementsArray = (ArrayNode) arrayNode.get(1);
                    return deserializeArrayElements(elementsArray, collectionClass);
                }
            } catch (ClassNotFoundException _) {
                // Not a wrapper format, fall through to legacy handling
            }
        }

        // Legacy format: [{"@class":"...", ...}, ...]
        return deserializeArrayElements(arrayNode, ArrayList.class);
    }

    private Collection<Object> deserializeArrayElements(ArrayNode arrayNode, Class<?> collectionClass) {
        Collection<Object> result = createCollection(collectionClass);

        for (JsonNode elementNode : arrayNode) {
            if (elementNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) elementNode;
                JsonNode classNode = objectNode.get(CLASS_PROPERTY);

                if (classNode != null && classNode.isString()) {
                    String className = classNode.asString();
                    try {
                        Class<?> elementClass = Class.forName(className);
                        // Remove @class property before deserializing
                        ObjectNode cleanNode = objectNode.deepCopy();
                        cleanNode.remove(CLASS_PROPERTY);
                        Object element = objectMapper.treeToValue(cleanNode, elementClass);
                        result.add(element);
                    } catch (ClassNotFoundException _) {
                        // If class not found, add as LinkedHashMap
                        result.add(objectMapper.treeToValue(objectNode, Object.class));
                    }
                } else {
                    // No @class property, deserialize as generic object
                    result.add(objectMapper.treeToValue(objectNode, Object.class));
                }
            } else if (elementNode.isArray() && elementNode.size() == 2 && elementNode.get(0).isString() && elementNode.get(1).isObject()) {
                // Handle wrapper array format for individual elements: ["com.example.Dto", {...}]
                String className = elementNode.get(0).asString();
                try {
                    Class<?> elementClass = Class.forName(className);
                    Object element = objectMapper.treeToValue(elementNode.get(1), elementClass);
                    result.add(element);
                } catch (ClassNotFoundException _) {
                    result.add(objectMapper.treeToValue(elementNode.get(1), Object.class));
                }
            } else {
                // Non-object elements (primitives, etc.)
                result.add(objectMapper.treeToValue(elementNode, Object.class));
            }
        }

        return result;
    }

    private Collection<Object> createCollection(Class<?> collectionClass) {
        if (Set.class.isAssignableFrom(collectionClass)) {
            if (TreeSet.class.isAssignableFrom(collectionClass)) {
                return new TreeSet<>();
            } else if (LinkedHashSet.class.isAssignableFrom(collectionClass)) {
                return new LinkedHashSet<>();
            } else {
                return new HashSet<>();
            }
        } else if (LinkedList.class.isAssignableFrom(collectionClass)) {
            return new LinkedList<>();
        }
        return new ArrayList<>();
    }

    private Object deserializeObject(byte[] bytes) {
        JsonNode rootNode = objectMapper.readTree(bytes);

        if (!rootNode.isObject()) {
            return objectMapper.readValue(bytes, Object.class);
        }

        ObjectNode objectNode = (ObjectNode) rootNode;
        JsonNode classNode = objectNode.get(CLASS_PROPERTY);

        if (classNode != null && classNode.isString()) {
            String className = classNode.asString();

            // Check if it's a Map type
            if (isMapType(className)) {
                return deserializeMap(objectNode, className);
            }

            try {
                Class<?> clazz = Class.forName(className);
                // Remove @class property before deserializing to avoid it being part of the target object
                ObjectNode cleanNode = objectNode.deepCopy();
                cleanNode.remove(CLASS_PROPERTY);
                return objectMapper.treeToValue(cleanNode, clazz);
            } catch (ClassNotFoundException _) {
                // If class not found, return as Map
                return objectMapper.treeToValue(objectNode, Object.class);
            }
        }

        // No @class property - could be a plain map, deserialize as generic object
        return deserializeAsMap(objectNode);
    }

    private boolean isMapType(String className) {
        return className.equals(HashMap.class.getName()) ||
                className.equals(LinkedHashMap.class.getName()) ||
                className.equals(TreeMap.class.getName()) ||
                className.equals(ConcurrentHashMap.class.getName()) ||
                className.startsWith("java.util.") && className.contains("Map");
    }

    private Map<String, Object> deserializeMap(ObjectNode objectNode, String className) {
        Map<String, Object> result = createMap(className);

        objectNode.properties().forEach(entry -> {
            String key = entry.getKey();
            // Skip the @class property
            if (!CLASS_PROPERTY.equals(key)) {
                JsonNode valueNode = entry.getValue();
                Object value = deserializeMapValue(valueNode);
                result.put(key, value);
            }
        });

        return result;
    }

    private Map<String, Object> deserializeAsMap(ObjectNode objectNode) {
        Map<String, Object> result = new LinkedHashMap<>();

        objectNode.properties()
                .forEach(entry -> {
                    String key = entry.getKey();
                    // Skip the @class property if present
                    if (!CLASS_PROPERTY.equals(key)) {
                        JsonNode valueNode = entry.getValue();
                        Object value = deserializeMapValue(valueNode);
                        result.put(key, value);
                    }
                });

        return result;
    }

    private Object deserializeMapValue(JsonNode valueNode) {
        try {
            if (valueNode.isObject()) {
                ObjectNode objNode = (ObjectNode) valueNode;
                JsonNode classNode = objNode.get(CLASS_PROPERTY);

                if (classNode != null && classNode.isString()) {
                    String className = classNode.asString();
                    if (isMapType(className)) {
                        return deserializeMap(objNode, className);
                    }
                    return getObject(className, objNode);
                }
                return objectMapper.treeToValue(objNode, Object.class);
            } else if (valueNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) valueNode;
                return deserializeArrayElements(arrayNode, ArrayList.class);
            } else {
                return objectMapper.treeToValue(valueNode, Object.class);
            }
        } catch (Exception e) {
            log.error("Error deserializing map value: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    private Object getObject(String className, ObjectNode objNode) {
        try {
            Class<?> clazz = Class.forName(className);
            return objectMapper.treeToValue(objNode, clazz);
        } catch (ClassNotFoundException _) {
            return objectMapper.treeToValue(objNode, Object.class);
        }
    }

    private Map<String, Object> createMap(String className) {
        if (className.equals(TreeMap.class.getName())) {
            return new TreeMap<>();
        } else if (className.equals(ConcurrentHashMap.class.getName())) {
            return new ConcurrentHashMap<>();
        } else if (className.equals(LinkedHashMap.class.getName())) {
            return new LinkedHashMap<>();
        }
        return new HashMap<>();
    }
}
