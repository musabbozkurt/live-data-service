package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.data.model.Coffee;
import com.mb.livedataservice.data.repository.CoffeeRepository;
import com.mb.livedataservice.enums.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coffee")
public class CoffeeController {

    private final CoffeeRepository coffeeRepository;

    @GetMapping
    public List<Coffee> getAllCoffees() {
        return coffeeRepository.findAll();
    }

    @GetMapping("/search")
    public List<Coffee> searchCoffeesByName(@RequestParam String pattern) {
        return coffeeRepository.findByNameContainingIgnoreCase(pattern);
    }

    @GetMapping("/filter")
    public List<Coffee> getCoffeesBySizeAndPrice(@RequestParam Size size,
                                                 @RequestParam BigDecimal minPrice) {
        return coffeeRepository.findBySizeAndPriceGreaterThan(size, minPrice);
    }

    @GetMapping("/affordable")
    public List<Coffee> getAffordableCoffees(@RequestParam(defaultValue = "LARGE") Size size,
                                             @RequestParam(defaultValue = "6.00") BigDecimal maxPrice) {
        return coffeeRepository.findAffordableCoffeesBySize(size.name(), maxPrice);
    }
}
