package com.mb.livedataservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.livedataservice.api.request.ApiScoreBoardRequest;
import com.mb.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.mb.livedataservice.api.response.ApiScoreBoardResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.data.model.ScoreBoard;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.exception.RestResponseExceptionHandler;
import com.mb.livedataservice.mapper.ScoreBoardMapper;
import com.mb.livedataservice.service.ScoreBoardService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {ScoreBoardController.class, RestResponseExceptionHandler.class})
class ScoreBoardControllerTest extends BaseUnitTest {

    @MockitoBean
    private ScoreBoardService scoreBoardService;

    @MockitoBean
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

        given(scoreBoardService.getAllScoreBoards(Mockito.any(PageRequest.class))).willReturn(scoreBoards);
        given(scoreBoardMapper.map(scoreBoards)).willReturn(apiScoreBoardResponses);

        mockMvc.perform(get("/score-boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(scoreBoards.getContent().size())))
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
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
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
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
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
