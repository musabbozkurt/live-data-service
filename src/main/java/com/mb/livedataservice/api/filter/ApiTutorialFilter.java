package com.mb.livedataservice.api.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiTutorialFilter {

    private String title;

    private String description;

    private boolean published;
}
