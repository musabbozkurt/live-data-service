package com.mb.livedataservice.data.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarFilter {

    private String model;

    private Integer yearOfManufacture;

    private String brand;
}
