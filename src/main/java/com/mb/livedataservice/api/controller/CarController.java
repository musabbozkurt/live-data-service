package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.ApiCarRequest;
import com.mb.livedataservice.api.response.ApiCarResponse;
import com.mb.livedataservice.mapper.CarMapper;
import com.mb.livedataservice.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;
    private final CarMapper carMapper;

    @PostMapping
    public ApiCarResponse create(@RequestBody ApiCarRequest car) {
        log.info("Received a request to create. create - ApiCarRequest: {}", car);
        return carMapper.map(carService.save(carMapper.map(car)));
    }

    @GetMapping("/{id}")
    public ApiCarResponse getCarById(@PathVariable String id) {
        log.info("Received a request to get car by id. getCarById - id: {}", id);
        return carMapper.map(carService.findById(id));
    }

    @GetMapping
    public Page<ApiCarResponse> findAll(Pageable pageable) {
        log.info("Received a request to find all. findAll - Pageable: {}", pageable);
        return carService.findAll(pageable).map(carMapper::map);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        log.info("Received a request to delete. delete - id: {}", id);
        carService.deleteCarById(id);
    }
}
