package com.sportradar.livedataservice.api.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NotNull
@ToString
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiQueueRequest {

    @NotNull(message = "Message content")
    @ApiModelProperty(value = "Message content", example = "Publishing a message")
    private String message;

}
