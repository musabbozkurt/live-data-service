package com.mb.livedataservice.util;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonUtils {

    // thread-safe!
    @Getter
    private static final ObjectMapper mapper = createMapper();

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

    public static ObjectMapper createMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .defaultTimeZone(TimeZone.getTimeZone("Europe/Istanbul"))
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .build();
    }

    public static <T> T convertValue(Object content, TypeReference<T> typeReference) {
        try {
            return mapper.convertValue(content, typeReference);
        } catch (Exception e) {
            log.error("Error occurred while converting value. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON convert value error, please check the logs for more details");
        }
    }

    public static <T> T readJSON(String content, Class<T> clazz) {
        try {
            return mapper.readValue(content, clazz);
        } catch (Exception e) {
            log.error("Error occurred while reading JSON. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON read value error, please check the logs for more details");
        }
    }

    public static <T> T readJSON(String content, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(content, typeReference);
        } catch (Exception e) {
            log.error("Error occurred while reading type referenced JSON. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON read value error, please check the logs for more details");
        }
    }

    public static <T> T treeToValue(Object map, Class<T> clazz) {
        try {
            return mapper.treeToValue(mapper.convertValue(map, JsonNode.class), clazz);
        } catch (Exception e) {
            log.error("Error occurred while reading tree to value. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON tree to value error, please check the logs for more details");
        }
    }

    public static <T> T toObject(Object map, Class<T> clazz) {
        if (null == map) {
            return null;
        }
        return treeToValue(map, clazz);
    }

    public static <T extends JsonNode> T valueToTree(Object value) {
        try {
            return mapper.valueToTree(value);
        } catch (Exception e) {
            log.error("Error occurred while converting value to JSON. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON value to tree error, please check the logs for more details");
        }
    }

    public static String writeJSON(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static <T> Collection<T> convertJSONCollection(String content, Class<T> clz) {
        try {
            return mapper.readValue(content, mapper.getTypeFactory().constructCollectionType(List.class, clz));
        } catch (Exception e) {
            log.error("Error occurred while converting JSON to Collection. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON convert collection error, please check the logs for more details");
        }
    }

    public static <T> Collection<T> readJSONCollection(String content, Class<T> clz) {
        try {
            Collection<T> result = new ArrayList<>();
            ArrayNode c = readJSON(content, ArrayNode.class);
            for (JsonNode jsonNode : c) {
                // For String class, handle text nodes directly
                if (clz == String.class && jsonNode.isString()) {
                    result.add((T) jsonNode.stringValue());
                } else {
                    // For complex objects or non-text nodes, convert to JSON string first
                    String jsonString = jsonNode.toString();
                    T node = mapper.readValue(jsonString, clz);
                    result.add(node);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error occurred while reading JSON collection. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON read collection error, please check the logs for more details");
        }
    }

    public static <T> Collection<T> readJSONCollection(ArrayNode content, Class<T> clz) {
        try {
            Collection<T> result = new ArrayList<>();
            for (JsonNode jsonNode : content) {
                if (clz == String.class) {
                    // For String class, get the string value based on node type
                    if (jsonNode.isString()) {
                        result.add((T) jsonNode.stringValue());
                    } else {
                        result.add((T) jsonNode.toString());
                    }
                } else {
                    T node = mapper.readValue(jsonNode.toString(), clz);
                    result.add(node);
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Error occurred while reading JSON collection from ArrayNode. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException("JSON read collection error, please check the logs for more details");
        }
    }

    public static String prettyPrint(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static boolean isJsonValid(String json) {
        try {
            if (StringUtils.isBlank(json) || json.trim().isEmpty()) {
                return false;
            }
            mapper.readTree(json);
        } catch (Exception _) {
            return false;
        }
        return true;
    }

    public static boolean isJsonNullOrValid(String json) {
        if (Objects.isNull(json)) {
            return true;
        } else if (json.trim().isEmpty()) {
            return false;
        } else {
            try {
                mapper.readTree(json);
            } catch (Exception _) {
                return false;
            }
            return true;
        }
    }
}
