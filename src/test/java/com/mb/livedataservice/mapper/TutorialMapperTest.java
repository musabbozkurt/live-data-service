package com.mb.livedataservice.mapper;

import com.mb.livedataservice.api.filter.ApiTutorialFilter;
import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.data.filter.TutorialFilter;
import com.mb.livedataservice.data.model.Tutorial;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TutorialMapperTest extends BaseUnitTest {

    TutorialMapper tutorialMapper = Mappers.getMapper(TutorialMapper.class);

    @Test
    void map_ApiTutorialRequestToTutorial_ShouldSucceed() {
        // arrange
        ApiTutorialRequest apiTutorialRequest = getApiTutorialRequest();

        // act
        Tutorial result = tutorialMapper.map(apiTutorialRequest);

        // assertion
        assertEquals(apiTutorialRequest.getTitle(), result.getTitle());
        assertEquals(apiTutorialRequest.getDescription(), result.getDescription());
        assertEquals(apiTutorialRequest.isPublished(), result.isPublished());
    }

    @Test
    void map_ApiTutorialUpdateRequestToTutorial_ShouldSucceed() {
        // arrange
        ApiTutorialUpdateRequest apiTutorialUpdateRequest = getApiTutorialUpdateRequest();

        // act
        Tutorial result = tutorialMapper.map(apiTutorialUpdateRequest);

        // assertion
        assertEquals(apiTutorialUpdateRequest.getTitle(), result.getTitle());
        assertEquals(apiTutorialUpdateRequest.getDescription(), result.getDescription());
        assertEquals(apiTutorialUpdateRequest.isPublished(), result.isPublished());
    }

    @Test
    void map_TutorialToApiTutorialResponse_ShouldSucceed() {
        // arrange
        Tutorial tutorial = getTutorial();

        // act
        ApiTutorialResponse result = tutorialMapper.map(tutorial);

        // assertion
        assertEquals(tutorial.getId(), result.getId());
        assertEquals(tutorial.getTitle(), result.getTitle());
        assertEquals(tutorial.getDescription(), result.getDescription());
        assertEquals(tutorial.isPublished(), result.isPublished());
    }

    @Test
    void map_ListOfTutorialToListOfApiTutorialResponse_ShouldSucceed() {
        // arrange
        List<Tutorial> tutorials = getTutorials();

        // act
        List<ApiTutorialResponse> result = tutorialMapper.map(tutorials);

        // assertion
        assertEquals(tutorials.getFirst().getId(), result.getFirst().getId());
        assertEquals(tutorials.getFirst().getTitle(), result.getFirst().getTitle());
        assertEquals(tutorials.getFirst().getDescription(), result.getFirst().getDescription());
        assertEquals(tutorials.getFirst().isPublished(), result.getFirst().isPublished());
    }

    @Test
    void map_ApiTutorialFilterToTutorialFilter_ShouldSucceed() {
        // arrange
        ApiTutorialFilter apiTutorialFilter = getApiTutorialFilter();

        // act
        TutorialFilter result = tutorialMapper.map(apiTutorialFilter);

        // assertion
        assertEquals(apiTutorialFilter.getTitle(), result.getTitle());
        assertEquals(apiTutorialFilter.getDescription(), result.getDescription());
        assertEquals(apiTutorialFilter.isPublished(), result.isPublished());
    }
}
