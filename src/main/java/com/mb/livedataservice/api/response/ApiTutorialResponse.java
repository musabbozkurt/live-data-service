package com.mb.livedataservice.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiTutorialResponse {

    private Long id;

    private String title;

    private String description;

    private boolean published;
}
