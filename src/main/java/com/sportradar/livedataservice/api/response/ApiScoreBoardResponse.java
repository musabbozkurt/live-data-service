package com.sportradar.livedataservice.api.response;

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

    private Long id;

    private OffsetDateTime createdDateTime;

    private OffsetDateTime modifiedDateTime;

    private String homeTeamName;

    private String awayTeamName;

    private int homeTeamScore;

    private int awayTeamScore;
}
