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

    private Long id;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime modifiedDateTime;

    @Schema(example = "TURKEY")
    private String homeTeamName;

    @Schema(example = "IRELAND")
    private String awayTeamName;

    @Schema(example = "0")
    private int homeTeamScore;

    @Schema(example = "0")
    private int awayTeamScore;
}
