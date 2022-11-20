package com.sportradar.livedataservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportradar.livedataservice.api.request.ApiScoreBoardRequest;
import com.sportradar.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.sportradar.livedataservice.api.response.ApiScoreBoardResponse;
import com.sportradar.livedataservice.base.BaseUnitTest;
import com.sportradar.livedataservice.data.model.ScoreBoard;
import com.sportradar.livedataservice.exception.BaseException;
import com.sportradar.livedataservice.exception.LiveDataErrorCode;
import com.sportradar.livedataservice.mapper.ScoreBoardMapper;
import com.sportradar.livedataservice.service.ScoreBoardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = ScoreBoardController.class)
class ScoreBoardControllerTests extends BaseUnitTest {
    @MockBean
    private ScoreBoardService scoreBoardService;

    @MockBean
    private ScoreBoardMapper scoreBoardMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createScoreBoard() throws Exception {
        ApiScoreBoardRequest apiScoreBoardRequest = getApiScoreBoardRequest();
        ScoreBoard scoreBoard = getScoreBoard();
        ApiScoreBoardResponse apiScoreBoardResponse = getApiScoreBoardResponse();

        when(scoreBoardMapper.map(apiScoreBoardRequest)).thenReturn(scoreBoard);
        when(scoreBoardService.createScoreBoard(scoreBoard)).thenReturn(scoreBoard);
        when(scoreBoardMapper.map(scoreBoard)).thenReturn(apiScoreBoardResponse);

        mockMvc.perform(post("/score-boards").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiScoreBoardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(apiScoreBoardResponse.getId()))
                .andExpect(jsonPath("$.homeTeamName").value(apiScoreBoardResponse.getHomeTeamName()))
                .andExpect(jsonPath("$.awayTeamName").value(apiScoreBoardResponse.getAwayTeamName()))
                .andExpect(jsonPath("$.homeTeamScore").value(apiScoreBoardResponse.getHomeTeamScore()))
                .andDo(print());
    }

    @Test
    void getAllScoreBoards() throws Exception {
        List<ScoreBoard> scoreBoardList = getScoreBoardList();
        List<ApiScoreBoardResponse> apiScoreBoardResponseList = getApiScoreBoardResponseList();

        PageImpl<ScoreBoard> scoreBoards = new PageImpl<>(scoreBoardList);
        PageImpl<ApiScoreBoardResponse> apiScoreBoardResponses = new PageImpl<>(apiScoreBoardResponseList);

        when(scoreBoardService.getAllScoreBoards(Pageable.unpaged())).thenReturn(scoreBoards);

        mockMvc.perform(get("/score-boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(scoreBoards.getContent().size()))
                .andDo(print());
    }

    @Test
    void getScoreBoardById() throws Exception {
        ScoreBoard scoreBoard = getScoreBoard();
        long id = scoreBoard.getId();
        ApiScoreBoardResponse apiScoreBoardResponse = getApiScoreBoardResponse();

        when(scoreBoardService.getScoreBoardById(id)).thenReturn(scoreBoard);
        when(scoreBoardMapper.map(scoreBoard)).thenReturn(apiScoreBoardResponse);

        mockMvc.perform(get("/score-boards/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(apiScoreBoardResponse.getId()))
                .andExpect(jsonPath("$.homeTeamName").value(apiScoreBoardResponse.getHomeTeamName()))
                .andExpect(jsonPath("$.awayTeamName").value(apiScoreBoardResponse.getAwayTeamName()))
                .andExpect(jsonPath("$.homeTeamScore").value(apiScoreBoardResponse.getHomeTeamScore()))
                .andDo(print());
    }

    @Test
    void shouldGetScoreBoardByIdThrowException_WhenScoreBoardNotFound() throws Exception {
        Long id = 1L;

        when(scoreBoardService.getScoreBoardById(id)).thenThrow(new BaseException(LiveDataErrorCode.SCORE_BOARD_NOT_FOUND));

        mockMvc.perform(get("/score-boards/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof BaseException))
                .andDo(print());
    }

    @Test
    void shouldUpdateScoreBoardById() throws Exception {
        long id = 1L;

        ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest = getApiScoreBoardUpdateRequest();
        ScoreBoard updatedScoreBoard = getUpdatedScoreBoard();

        ApiScoreBoardResponse updatedApiScoreBoardResponse = getUpdatedApiScoreBoardResponse();

        when(scoreBoardMapper.map(apiScoreBoardUpdateRequest)).thenReturn(updatedScoreBoard);
        when(scoreBoardService.updateScoreBoardById(id, updatedScoreBoard)).thenReturn(updatedScoreBoard);
        when(scoreBoardMapper.map(updatedScoreBoard)).thenReturn(updatedApiScoreBoardResponse);

        mockMvc.perform(put("/score-boards/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiScoreBoardUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedApiScoreBoardResponse.getId()))
                .andExpect(jsonPath("$.homeTeamName").value(updatedApiScoreBoardResponse.getHomeTeamName()))
                .andExpect(jsonPath("$.awayTeamName").value(updatedApiScoreBoardResponse.getAwayTeamName()))
                .andExpect(jsonPath("$.homeTeamScore").value(updatedApiScoreBoardResponse.getHomeTeamScore()))
                .andDo(print());
    }

    @Test
    void shouldUpdateScoreBoardByIdThrowException_WhenScoreBoardNotFound() throws Exception {
        long id = 3L;

        ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest = getApiScoreBoardUpdateRequest();
        ScoreBoard updatedScoreBoard = getUpdatedScoreBoard();

        when(scoreBoardMapper.map(apiScoreBoardUpdateRequest)).thenReturn(updatedScoreBoard);
        when(scoreBoardService.updateScoreBoardById(id, updatedScoreBoard)).thenThrow(new BaseException(LiveDataErrorCode.SCORE_BOARD_NOT_FOUND));

        mockMvc.perform(put("/score-boards/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiScoreBoardUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertTrue(result.getResolvedException() instanceof BaseException))
                .andDo(print());
    }

    @Test
    void shouldRemoveScoreBoardById() throws Exception {
        long id = 1L;

        doNothing().when(scoreBoardService).removeScoreBoardById(id);

        mockMvc.perform(delete("/score-boards/{id}", id))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void shouldGetAllScoreBoardsInAscendingOrderByModifiedDateTime() throws Exception {
        List<String> scoreBoardsAsStringList = getScoreBoardsAsStringList();

        when(scoreBoardService.getAllScoreBoardsInAscendingOrderByModifiedDateTime()).thenReturn(scoreBoardsAsStringList);

        mockMvc.perform(get("/score-boards/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(scoreBoardsAsStringList.size()))
                .andDo(print());
    }
}
