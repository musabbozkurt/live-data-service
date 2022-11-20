package com.sportradar.livedataservice.base;

import com.sportradar.livedataservice.api.request.ApiScoreBoardRequest;
import com.sportradar.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.sportradar.livedataservice.api.response.ApiScoreBoardResponse;
import com.sportradar.livedataservice.data.model.ScoreBoard;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class BaseUnitTest {

    public ScoreBoard getScoreBoard() {
        ScoreBoard scoreBoard = new ScoreBoard();
        scoreBoard.setId(new Random().nextLong());
        scoreBoard.setHomeTeamName("TURKEY");
        scoreBoard.setAwayTeamName("IRELAND");
        scoreBoard.setHomeTeamScore(0);
        scoreBoard.setAwayTeamScore(0);
        return scoreBoard;
    }

    public ScoreBoard getScoreBoardForUpdate() {
        ScoreBoard scoreBoard = new ScoreBoard();
        scoreBoard.setHomeTeamName("TURKEY");
        scoreBoard.setAwayTeamName("IRELAND");
        scoreBoard.setHomeTeamScore(0);
        scoreBoard.setAwayTeamScore(1);
        return scoreBoard;
    }

    public ApiScoreBoardRequest getApiScoreBoardRequest() {
        ApiScoreBoardRequest scoreBoardRequest = new ApiScoreBoardRequest();
        scoreBoardRequest.setHomeTeamName("TURKEY");
        scoreBoardRequest.setAwayTeamName("IRELAND");
        scoreBoardRequest.setHomeTeamScore(0);
        scoreBoardRequest.setAwayTeamScore(0);
        return scoreBoardRequest;
    }

    public ApiScoreBoardUpdateRequest getApiScoreBoardUpdateRequest() {
        return new ApiScoreBoardUpdateRequest("UruguayUpdated", "ItalyUpdated", 6, 6);
    }

    public ScoreBoard getUpdatedScoreBoard() {
        return new ScoreBoard("UruguayUpdated", "ItalyUpdated", 6, 6);
    }

    public ApiScoreBoardResponse getUpdatedApiScoreBoardResponse() {
        return new ApiScoreBoardResponse(1L, OffsetDateTime.now(), OffsetDateTime.now(), "UruguayUpdated", "ItalyUpdated", 6, 6);
    }

    public ApiScoreBoardResponse getApiScoreBoardResponse() {
        return new ApiScoreBoardResponse(1L, OffsetDateTime.now(), OffsetDateTime.now(), "Uruguay", "Italy", 6, 6);
    }

    public List<ScoreBoard> getScoreBoardList() {
        ScoreBoard scoreBoard = new ScoreBoard("Uruguay", "Italy", 6, 6);
        scoreBoard.setId(1L);

        ScoreBoard scoreBoard1 = new ScoreBoard("Spain", "Brazil", 10, 2);
        scoreBoard1.setId(2L);

        ScoreBoard scoreBoard2 = new ScoreBoard("Mexico", "Canada", 0, 5);
        scoreBoard2.setId(3L);

        ScoreBoard scoreBoard3 = new ScoreBoard("Argentina", "Australia", 3, 1);
        scoreBoard3.setId(4L);

        ScoreBoard scoreBoard4 = new ScoreBoard("Germany", "France", 2, 2);
        scoreBoard4.setId(5L);
        return Arrays.asList(scoreBoard, scoreBoard1, scoreBoard2, scoreBoard3, scoreBoard4);
    }

    public List<ApiScoreBoardResponse> getApiScoreBoardResponseList() {
        return Arrays.asList(new ApiScoreBoardResponse(1L, OffsetDateTime.now(), OffsetDateTime.now(), "Uruguay", "Italy", 6, 6),
                new ApiScoreBoardResponse(2L, OffsetDateTime.now(), OffsetDateTime.now(), "Spain", "Brazil", 10, 2),
                new ApiScoreBoardResponse(3L, OffsetDateTime.now(), OffsetDateTime.now(), "Mexico", "Canada", 0, 5),
                new ApiScoreBoardResponse(4L, OffsetDateTime.now(), OffsetDateTime.now(), "Argentina", "Australia", 3, 1),
                new ApiScoreBoardResponse(5L, OffsetDateTime.now(), OffsetDateTime.now(), "Germany", "France", 2, 2));
    }

    public List<String> getScoreBoardsAsStringList() {
        return Arrays.asList("1. Uruguay - Italy : 6 - 6",
                "2. Spain - Brazil : 10 - 2",
                "3. Mexico - Canada : 0 - 5",
                "4. Argentina - Australia : 3 - 1",
                "5. Germany - France : 2 - 2");
    }

}
