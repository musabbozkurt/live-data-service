package com.mb.livedataservice.data.model.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Test DTO for basic object serialization tests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestPersonDto implements Serializable {

    private Long id;
    private String name;
    private String email;
    private Integer age;
    private Boolean active;
    private LocalDate birthDate;
    private LocalDateTime createdAt;
}
