package com.mb.livedataservice.data.model.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test DTO with nested object and nested collections for complex serialization tests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCompanyDto implements Serializable {

    private Long id;
    private String name;
    private String industry;

    // Nested object
    private TestAddressDto headquarters;

    // Nested collection - List
    private List<TestPersonDto> employees;

    // Nested collection - Set
    private Set<String> departments;

    // Nested collection - Map
    private Map<String, TestPersonDto> managersById;

    // Nested collection of nested objects
    private List<TestAddressDto> branchAddresses;
}
