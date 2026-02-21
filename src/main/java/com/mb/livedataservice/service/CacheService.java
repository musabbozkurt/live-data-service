package com.mb.livedataservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CacheService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public boolean hasKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void put(String key, Object value) {
        if (value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value);
    }

    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (value == null) {
            return;
        }
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public <T> T get(String key, Class<T> clazz) {
        return objectMapper.convertValue(redisTemplate.opsForValue().get(key), clazz);
    }

    public HashOperations<String, String, String> getHashOps() {
        return redisTemplate.opsForHash();
    }

    public <T> Collection<T> get(String key, Class<?> collectionType, Class<T> elementType) {
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue == null) {
            if (Set.class.isAssignableFrom(collectionType)) {
                return Collections.emptySet();
            }
            return Collections.emptyList();
        }

        // Always convert to List first since CustomJackson2JsonRedisSerializer deserializes arrays as ArrayList
        List<T> listResult = objectMapper.convertValue(cachedValue, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementType));

        if (List.class.isAssignableFrom(collectionType)) {
            return listResult;
        } else if (Set.class.isAssignableFrom(collectionType)) {
            // Convert List to Set
            return new HashSet<>(listResult);
        } else {
            return listResult;
        }
    }

    public <K, V> Map<K, V> getMap(String key, Class<K> keyType, Class<V> valueType) {
        Object cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue == null) {
            return Collections.emptyMap();
        }

        return objectMapper.convertValue(cachedValue, objectMapper.getTypeFactory().constructMapType(HashMap.class, keyType, valueType));
    }

    public Set<String> getKeys(String prefix) {
        return redisTemplate.keys(prefix);
    }

    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public boolean deleteAll(Set<String> keys) {
        Long result = redisTemplate.delete(keys);
        return result != null && result.intValue() == keys.size();
    }
}
