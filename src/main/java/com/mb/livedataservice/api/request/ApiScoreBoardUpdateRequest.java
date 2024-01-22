package com.mb.livedataservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@Builder
@NotNull
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiScoreBoardUpdateRequest {

    @NotNull(message = "Home Team Name is mandatory")
    @Schema(name = "Home Team Name", example = "TURKEY")
    private String homeTeamName;

    @NotNull(message = "Away Team Name is mandatory")
    @Schema(name = "Away Team Name", example = "IRELAND")
    private String awayTeamName;

    @Builder.Default
    @NotNull(message = "Home Team Score is mandatory")
    @Schema(name = "Home Team Score", example = "0")
    private int homeTeamScore = 0;

    @Builder.Default
    @NotNull(message = "Away Team Score is mandatory")
    @Schema(name = "Away Team Score", example = "0")
    private int awayTeamScore = 0;
}
