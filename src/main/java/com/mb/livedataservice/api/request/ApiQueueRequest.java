package com.mb.livedataservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@NotNull
@ToString
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiQueueRequest {

    @NotNull
    @Schema(example = "Publishing a message")
    private String message;
}
