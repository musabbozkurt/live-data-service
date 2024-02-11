package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.elastic.Car;
import com.mb.livedataservice.data.repository.CarRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;

    @Override
    public Car save(Car car) {
        return carRepository.save(car);
    }

    @Override
    public Car findById(String id) {
        return carRepository.findById(id).orElseThrow(() -> new BaseException(LiveDataErrorCode.NOT_FOUND));
    }

    @Override
    public Page<Car> findAll(Pageable pageable) {
        return carRepository.findAll(pageable);
    }

    @Override
    public void deleteCarById(String id) {
        carRepository.deleteById(id);
    }
}
