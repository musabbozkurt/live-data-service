package com.mb.livedataservice.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.time.OffsetDateTime;

@Data
@Builder
@NotNull
@Validated
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiScoreBoardResponse {

    @Schema(name = "Id of score board")
    private Long id;

    @Schema(name = "Created date of score board")
    private OffsetDateTime createdDateTime;

    @Schema(name = "Last modified date of score board")
    private OffsetDateTime modifiedDateTime;

    @Schema(name = "Home Team Name", example = "TURKEY")
    private String homeTeamName;

    @Schema(name = "Away Team Name", example = "IRELAND")
    private String awayTeamName;

    @Schema(name = "Home Team Score", example = "0")
    private int homeTeamScore;

    @Schema(name = "Away Team Score", example = "0")
    private int awayTeamScore;
}
