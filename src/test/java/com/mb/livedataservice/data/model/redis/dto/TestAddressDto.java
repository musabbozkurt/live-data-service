package com.mb.livedataservice.data.model.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Test DTO for address - used as nested object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAddressDto implements Serializable {

    private Long id;
    private String street;
    private String city;
    private String country;
    private String zipCode;
}
