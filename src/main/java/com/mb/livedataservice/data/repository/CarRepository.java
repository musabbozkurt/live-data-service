package com.mb.livedataservice.data.repository;

import com.mb.livedataservice.data.model.elastic.Car;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends ElasticsearchRepository<Car, String> {

}
