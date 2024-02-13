package com.mb.livedataservice.service;

import com.mb.livedataservice.data.model.elastic.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    Car save(Car car);

    Car findById(String id);

    Page<Car> findAll(Pageable pageable);

    void deleteCarById(String id);
}