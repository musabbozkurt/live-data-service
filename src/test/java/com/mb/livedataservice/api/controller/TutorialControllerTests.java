package com.mb.livedataservice.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.data.model.Tutorial;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.exception.RestResponseExceptionHandler;
import com.mb.livedataservice.mapper.TutorialMapper;
import com.mb.livedataservice.service.TutorialService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;

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
@ContextConfiguration(classes = {TutorialController.class, RestResponseExceptionHandler.class})
class TutorialControllerTests extends BaseUnitTest {

    @MockitoBean
    private TutorialService tutorialService;

    @MockitoBean
    private TutorialMapper tutorialMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateTutorial() throws Exception {
        ApiTutorialRequest apiTutorialRequest = getApiTutorialRequest();
        Tutorial tutorial = getTutorial();
        ApiTutorialResponse apiTutorialResponse = getApiTutorialResponse();

        when(tutorialMapper.map(apiTutorialRequest)).thenReturn(tutorial);
        when(tutorialService.save(tutorial)).thenReturn(tutorial);
        when(tutorialMapper.map(tutorial)).thenReturn(apiTutorialResponse);

        mockMvc.perform(post("/api/tutorials").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiTutorialRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(apiTutorialResponse.getId()))
                .andExpect(jsonPath("$.title").value(apiTutorialResponse.getTitle()))
                .andExpect(jsonPath("$.description").value(apiTutorialResponse.getDescription()))
                .andExpect(jsonPath("$.published").value(apiTutorialResponse.isPublished()))
                .andDo(print());
    }

    @Test
    void shouldReturnTutorial() throws Exception {
        long id = 1L;
        Tutorial tutorial = getTutorial();
        ApiTutorialResponse apiTutorialResponse = getApiTutorialResponse();

        when(tutorialService.findById(id)).thenReturn(tutorial);
        when(tutorialMapper.map(tutorial)).thenReturn(apiTutorialResponse);

        mockMvc.perform(get("/api/tutorials/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value(apiTutorialResponse.getTitle()))
                .andExpect(jsonPath("$.description").value(apiTutorialResponse.getDescription()))
                .andExpect(jsonPath("$.published").value(apiTutorialResponse.isPublished()))
                .andDo(print());
    }

    @Test
    void shouldThrowException_WhenTutorialNotFound() throws Exception {
        long id = 1L;

        when(tutorialService.findById(id)).thenThrow(new BaseException(LiveDataErrorCode.NOT_FOUND));

        mockMvc.perform(get("/api/tutorials/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }

    @Test
    void shouldReturnListOfTutorials() throws Exception {
        List<Tutorial> tutorials = getTutorials();

        List<ApiTutorialResponse> apiTutorialResponses = getApiTutorialResponses();

        when(tutorialService.findByTitleContaining(null)).thenReturn(tutorials);
        when(tutorialMapper.map(tutorials)).thenReturn(apiTutorialResponses);

        mockMvc.perform(get("/api/tutorials"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(apiTutorialResponses.size()))
                .andDo(print());
    }

    @Test
    void shouldReturnListOfTutorialsWithFilter() throws Exception {
        List<Tutorial> tutorials = getTutorialList();

        List<ApiTutorialResponse> apiTutorialResponses = getApiTutorialResponseList();

        String title = "Boot";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("title", title);

        when(tutorialService.findByTitleContaining(title)).thenReturn(tutorials);
        when(tutorialMapper.map(tutorials)).thenReturn(apiTutorialResponses);

        mockMvc.perform(get("/api/tutorials").params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(apiTutorialResponses.size()))
                .andDo(print());
    }

    @Test
    void shouldReturnNoContentWhenFilter() throws Exception {
        String title = "mb";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("title", title);

        List<Tutorial> tutorials = Collections.emptyList();

        when(tutorialService.findByTitleContaining(title)).thenReturn(tutorials);
        when(tutorialMapper.map(tutorials)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tutorials").params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0))
                .andDo(print());
    }

    @Test
    void shouldUpdateTutorial() throws Exception {
        long id = 1L;

        ApiTutorialUpdateRequest apiTutorialUpdateRequest = getApiTutorialUpdateRequest();
        Tutorial updatedTutorial = getUpdatedTutorial();

        ApiTutorialResponse apiTutorialResponse = getUpdatedApiTutorialResponse();

        when(tutorialMapper.map(apiTutorialUpdateRequest)).thenReturn(updatedTutorial);
        when(tutorialService.update(id, updatedTutorial)).thenReturn(updatedTutorial);
        when(tutorialMapper.map(updatedTutorial)).thenReturn(apiTutorialResponse);

        mockMvc.perform(put("/api/tutorials/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiTutorialUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(apiTutorialResponse.getTitle()))
                .andExpect(jsonPath("$.description").value(apiTutorialResponse.getDescription()))
                .andExpect(jsonPath("$.published").value(apiTutorialResponse.isPublished()))
                .andDo(print());
    }

    @Test
    void shouldReturnNotFoundUpdateTutorial() throws Exception {
        long id = 1L;

        ApiTutorialUpdateRequest apiTutorialUpdateRequest = getApiTutorialUpdateRequest();
        Tutorial updatedTutorial = getUpdatedTutorial();

        when(tutorialMapper.map(apiTutorialUpdateRequest)).thenReturn(updatedTutorial);
        when(tutorialService.update(id, updatedTutorial)).thenThrow(new BaseException(LiveDataErrorCode.NOT_FOUND));

        mockMvc.perform(put("/api/tutorials/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apiTutorialUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(BaseException.class, result.getResolvedException()))
                .andDo(print());
    }

    @Test
    void shouldDeleteTutorial() throws Exception {
        long id = 1L;

        doNothing().when(tutorialService).deleteById(id);
        mockMvc.perform(delete("/api/tutorials/{id}", id))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    void shouldDeleteAllTutorials() throws Exception {
        doNothing().when(tutorialService).deleteAll();
        mockMvc.perform(delete("/api/tutorials"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    void shouldReturnListOfTutorials_WhenPublishedIsTrue() throws Exception {
        List<Tutorial> tutorials = getTutorials();

        List<ApiTutorialResponse> apiTutorialResponses = getApiTutorialResponses();

        when(tutorialService.findByPublished(true)).thenReturn(tutorials);
        when(tutorialMapper.map(tutorials)).thenReturn(apiTutorialResponses);

        mockMvc.perform(get("/api/tutorials/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(apiTutorialResponses.size()))
                .andDo(print());
    }
}
