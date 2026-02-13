package com.mb.livedataservice.data.model.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Test DTO with deeply nested collections for complex serialization tests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestDepartmentDto implements Serializable {

    private Long id;
    private String name;

    // List of Lists (nested collection)
    private List<List<String>> teamGroups;

    // Map with List values
    private Map<String, List<TestPersonDto>> teamMembers;

    // List of Maps
    private List<Map<String, String>> configurations;
}
