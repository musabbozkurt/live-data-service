package com.mb.livedataservice.api.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Builder
@NotNull
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiScoreBoardResponse {

    @ApiModelProperty(value = "Id of score board")
    private Long id;

    @ApiModelProperty(value = "Created date of score board")
    private OffsetDateTime createdDateTime;

    @ApiModelProperty(value = "Last modified date of score board")
    private OffsetDateTime modifiedDateTime;

    @ApiModelProperty(value = "Home Team Name", example = "TURKEY")
    private String homeTeamName;

    @ApiModelProperty(value = "Away Team Name", example = "IRELAND")
    private String awayTeamName;

    @ApiModelProperty(value = "Home Team Score", example = "0")
    private int homeTeamScore;

    @ApiModelProperty(value = "Away Team Score", example = "0")
    private int awayTeamScore;
}
