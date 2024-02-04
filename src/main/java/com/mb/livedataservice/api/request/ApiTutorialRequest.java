package com.mb.livedataservice.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiTutorialRequest {

    private String title;

    private String description;

    private boolean published;
}
