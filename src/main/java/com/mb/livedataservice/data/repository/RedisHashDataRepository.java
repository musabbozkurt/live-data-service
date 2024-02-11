package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.redis.RedisHashData;
import org.springframework.data.repository.CrudRepository;

public interface RedisHashDataRepository extends CrudRepository<RedisHashData, String> {

}
