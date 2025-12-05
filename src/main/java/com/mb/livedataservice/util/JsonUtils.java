package com.mb.livedataservice.util;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serialize(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static <T> T deserialize(String content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        if (clazz == String.class) {
            return (T) content;
        }
        try {
            return mapper.readValue(content, clazz);
        } catch (Exception _) {
            throw new BaseException(LiveDataErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public static <T> T deserialize(String content, TypeReference<T> typeReference) {
        if (content == null) {
            return null;
        }
        if (typeReference.getType() == String.class) {
            return (T) content;
        }
        try {
            return mapper.readValue(content, typeReference);
        } catch (Exception _) {
            throw new BaseException(LiveDataErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public static <T> T deserialize(JsonNode node, Class<T> clazz) {
        try {
            return mapper.treeToValue(node, clazz);
        } catch (Exception _) {
            throw new BaseException(LiveDataErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public static <T> T deserialize(String content, Type type) {
        try {
            return mapper.readValue(content, mapper.getTypeFactory().constructType(type));
        } catch (Exception _) {
            throw new BaseException(LiveDataErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public static JsonNode deserialize(String content) {
        try {
            return mapper.readTree(content);
        } catch (Exception _) {
            throw new BaseException(LiveDataErrorCode.CANNOT_MAP_RESPONSE);
        }
    }

    public static <T> T convert(Object object, Class<T> clazz) {
        return mapper.convertValue(object, clazz);
    }
}
