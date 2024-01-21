package com.mb.livedataservice.api.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NotNull
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiScoreBoardUpdateRequest {

    @NotNull(message = "Home Team Name is mandatory")
    @ApiModelProperty(value = "Home Team Name", example = "TURKEY")
    private String homeTeamName;

    @NotNull(message = "Away Team Name is mandatory")
    @ApiModelProperty(value = "Away Team Name", example = "IRELAND")
    private String awayTeamName;

    @NotNull(message = "Home Team Score is mandatory")
    @ApiModelProperty(value = "Home Team Score", example = "0")
    private int homeTeamScore = 0;

    @NotNull(message = "Away Team Score is mandatory")
    @ApiModelProperty(value = "Away Team Score", example = "0")
    private int awayTeamScore = 0;
}
