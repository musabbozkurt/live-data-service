package com.mb.livedataservice.service;

import com.mb.livedataservice.data.redis.model.RedisHashData;

import java.util.Optional;

public interface RedisHashService {

    RedisHashData save(RedisHashData redisHashData);

    Optional<RedisHashData> findById(String id);

    void delete(RedisHashData redisHashData);

    void deleteRedisHashDataById(String redisHashDataId);
}
