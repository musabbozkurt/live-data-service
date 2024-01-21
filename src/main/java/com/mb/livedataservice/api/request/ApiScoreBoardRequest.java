package com.mb.livedataservice.api.request;

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
public class ApiScoreBoardRequest {

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
