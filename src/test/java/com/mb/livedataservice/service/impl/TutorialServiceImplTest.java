package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.base.BaseUnitTest;
import com.mb.livedataservice.data.model.Tutorial;
import com.mb.livedataservice.data.repository.TutorialRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TutorialServiceImplTest extends BaseUnitTest {

    @Mock
    private TutorialRepository tutorialRepository;

    @InjectMocks
    private TutorialServiceImpl tutorialService;

    @Test
    void shouldFindByTitleContaining_WhenTitleIsNotBlank() {
        List<Tutorial> tutorialList = getTutorials();

        when(tutorialRepository.findByTitleContaining(anyString())).thenReturn(tutorialList);

        List<Tutorial> result = tutorialService.findByTitleContaining("title");

        Assertions.assertEquals(tutorialList, result);
        Assertions.assertEquals(tutorialList.size(), result.size());
    }

    @Test
    void shouldFindByTitleContaining_WhenTitleIsBlank() {
        List<Tutorial> tutorialList = getTutorials();

        when(tutorialRepository.findAll()).thenReturn(tutorialList);

        List<Tutorial> result = tutorialService.findByTitleContaining(null);

        Assertions.assertEquals(tutorialList, result);
        Assertions.assertEquals(tutorialList.size(), result.size());
    }

    @Test
    void shouldFindById_WhenTutorialIsFound() {
        Tutorial tutorial = getTutorial();

        when(tutorialRepository.findById(anyLong())).thenReturn(Optional.of(tutorial));

        Tutorial result = tutorialService.findById(1L);

        Assertions.assertEquals(tutorial, result);
    }

    @Test
    void shouldFindByIdThrowException_WhenTutorialIsNotFound() {
        when(tutorialRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> tutorialService.findById(1L));

        // Assertions
        Assertions.assertEquals(LiveDataErrorCode.NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void shouldSave() {
        Tutorial tutorial = getTutorial();

        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(tutorial);

        Tutorial result = tutorialService.save(tutorial);

        Assertions.assertEquals(tutorial, result);
    }

    @Test
    void shouldUpdate() {
        Tutorial tutorial = getTutorial();

        when(tutorialRepository.findById(1L)).thenReturn(Optional.of(tutorial));
        when(tutorialRepository.save(any(Tutorial.class))).thenReturn(tutorial);

        Tutorial result = tutorialService.update(1L, tutorial);

        Assertions.assertEquals(tutorial, result);
    }

    @Test
    void shouldUpdateThrowException_WhenTutorialIsNotFound() {
        Tutorial tutorial = getTutorial();

        when(tutorialRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> tutorialService.update(1L, tutorial));

        // Assertions
        Assertions.assertEquals(LiveDataErrorCode.NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void shouldDeleteById() {
        long tutorialId = new Random().nextLong();

        // Act
        tutorialService.deleteById(tutorialId);

        // Assertions
        verify(tutorialRepository).deleteById(tutorialId);
    }


    @Test
    void shouldDeleteAll() {
        tutorialService.deleteAll();

        verify(tutorialRepository).deleteAll();
    }

    @Test
    void shouldFindByPublished_WhenPublishedIsTrue() {
        List<Tutorial> tutorials = getTutorials();

        when(tutorialRepository.findByPublished(true)).thenReturn(tutorials);

        List<Tutorial> result = tutorialService.findByPublished(true);

        Assertions.assertEquals(tutorials, result);
        Assertions.assertEquals(tutorials.size(), result.size());
    }

    @Test
    void shouldFindByPublished_WhenPublishedIsFalse() {
        List<Tutorial> tutorials = getTutorials();

        when(tutorialRepository.findByPublished(false)).thenReturn(tutorials);

        List<Tutorial> result = tutorialService.findByPublished(false);

        Assertions.assertEquals(tutorials, result);
        Assertions.assertEquals(tutorials.size(), result.size());
    }
}