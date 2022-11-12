package com.sportradar.livedataservice.mapper;

import com.sportradar.livedataservice.api.request.ApiScoreBoardRequest;
import com.sportradar.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.sportradar.livedataservice.base.BaseUnitTest;
import com.sportradar.livedataservice.api.response.ApiScoreBoardResponse;
import com.sportradar.livedataservice.data.model.ScoreBoard;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreBoardMapperTest extends BaseUnitTest {

    ScoreBoardMapper scoreBoardMapper = Mappers.getMapper(ScoreBoardMapper.class);

    @Test
    void map_ApiScoreBoardRequestToScoreBoard_ShouldSucceed() {
        // arrange
        ApiScoreBoardRequest apiScoreBoardRequest = getApiScoreBoardRequest();

        // act
        ScoreBoard result = scoreBoardMapper.map(apiScoreBoardRequest);

        // assertion
        assertEquals(apiScoreBoardRequest.getHomeTeamName(), result.getHomeTeamName());
        assertEquals(apiScoreBoardRequest.getAwayTeamName(), result.getAwayTeamName());
        assertEquals(apiScoreBoardRequest.getHomeTeamScore(), result.getHomeTeamScore());
        assertEquals(apiScoreBoardRequest.getAwayTeamScore(), result.getAwayTeamScore());
    }

    @Test
    void map_ScoreBoardToApiScoreBoardResponse_ShouldSucceed() {
        // arrange
        ScoreBoard scoreBoard = getScoreBoard();

        // act
        ApiScoreBoardResponse result = scoreBoardMapper.map(scoreBoard);

        // assertion
        assertEquals(scoreBoard.getHomeTeamName(), result.getHomeTeamName());
        assertEquals(scoreBoard.getAwayTeamName(), result.getAwayTeamName());
        assertEquals(scoreBoard.getHomeTeamScore(), result.getHomeTeamScore());
        assertEquals(scoreBoard.getAwayTeamScore(), result.getAwayTeamScore());
    }

    @Test
    void map_ApiScoreBoardUpdateRequestToScoreBoard_ShouldSucceed() {
        // arrange
        ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest = getApiScoreBoardUpdateRequest();

        // act
        ScoreBoard result = scoreBoardMapper.map(apiScoreBoardUpdateRequest);

        // assertion
        assertEquals(apiScoreBoardUpdateRequest.getHomeTeamName(), result.getHomeTeamName());
        assertEquals(apiScoreBoardUpdateRequest.getAwayTeamName(), result.getAwayTeamName());
        assertEquals(apiScoreBoardUpdateRequest.getHomeTeamScore(), result.getHomeTeamScore());
        assertEquals(apiScoreBoardUpdateRequest.getAwayTeamScore(), result.getAwayTeamScore());
    }
}
