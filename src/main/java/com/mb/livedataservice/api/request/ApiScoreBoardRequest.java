package com.mb.livedataservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@NotNull
@ToString
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiScoreBoardRequest {

    @NotNull
    @Schema(example = "TURKEY")
    private String homeTeamName;

    @NotNull
    @Schema(example = "IRELAND")
    private String awayTeamName;

    @NotNull
    @Builder.Default
    @Schema(example = "0")
    private int homeTeamScore = 0;

    @NotNull
    @Builder.Default
    @Schema(example = "0")
    private int awayTeamScore = 0;
}
