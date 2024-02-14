package com.mb.livedataservice.api.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCarFilter {

    private String model;

    private Integer yearOfManufacture;

    private String brand;
}
