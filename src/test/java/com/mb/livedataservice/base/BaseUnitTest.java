package com.mb.livedataservice.base;

import com.mb.livedataservice.api.request.ApiScoreBoardRequest;
import com.mb.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiScoreBoardResponse;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.data.model.ScoreBoard;
import com.mb.livedataservice.data.model.Tutorial;

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

    public ApiTutorialRequest getApiTutorialRequest() {
        return new ApiTutorialRequest("Spring Boot @WebMvcTest", "Description", true);
    }

    public Tutorial getTutorial() {
        return new Tutorial(1, "Spring Boot @WebMvcTest", "Description", true);
    }

    public ApiTutorialResponse getApiTutorialResponse() {
        return new ApiTutorialResponse(1, "Spring Boot @WebMvcTest", "Description", true);
    }

    public List<Tutorial> getTutorials() {
        return Arrays.asList(new Tutorial(1, "Spring Boot @WebMvcTest 1", "Description 1", true),
                new Tutorial(2, "Spring Boot @WebMvcTest 2", "Description 2", true),
                new Tutorial(3, "Spring Boot @WebMvcTest 3", "Description 3", true));
    }

    public List<ApiTutorialResponse> getApiTutorialResponses() {
        return Arrays.asList(new ApiTutorialResponse(1, "Spring Boot @WebMvcTest 1", "Description 1", true),
                new ApiTutorialResponse(2, "Spring Boot @WebMvcTest 2", "Description 2", true),
                new ApiTutorialResponse(3, "Spring Boot @WebMvcTest 3", "Description 3", true));
    }

    public List<Tutorial> getTutorialList() {
        return Arrays.asList(new Tutorial(1, "Spring Boot @WebMvcTest", "Description 1", true),
                new Tutorial(3, "Spring Boot Web MVC", "Description 3", true));
    }

    public List<ApiTutorialResponse> getApiTutorialResponseList() {
        return Arrays.asList(new ApiTutorialResponse(1, "Spring Boot @WebMvcTest", "Description 1", true),
                new ApiTutorialResponse(3, "Spring Boot Web MVC", "Description 3", true));
    }

    public ApiTutorialUpdateRequest getApiTutorialUpdateRequest() {
        return new ApiTutorialUpdateRequest("Updated", "Updated", true);
    }

    public Tutorial getUpdatedTutorial() {
        return new Tutorial(1, "Updated", "Updated", true);
    }

    public ApiTutorialResponse getUpdatedApiTutorialResponse() {
        return new ApiTutorialResponse(1, "Updated", "Updated", true);
    }
}
