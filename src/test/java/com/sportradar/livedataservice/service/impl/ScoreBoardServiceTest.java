package com.sportradar.livedataservice.service.impl;

import com.sportradar.livedataservice.base.BaseUnitTest;
import com.sportradar.livedataservice.data.model.ScoreBoard;
import com.sportradar.livedataservice.data.repository.ScoreBoardRepository;
import com.sportradar.livedataservice.exception.BaseException;
import com.sportradar.livedataservice.exception.LiveDataErrorCode;
import com.sportradar.livedataservice.service.ScoreBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreBoardServiceTest extends BaseUnitTest {

    private ScoreBoardService service;

    @Mock
    private ScoreBoardRepository repository;


    @BeforeEach
    void setUp() {
        service = new ScoreBoardServiceImpl(repository);
    }

    @Test
    void createScoreBoard() {
        // Arrange
        // Act
        service.createScoreBoard(new ScoreBoard());

        // Assertions
        ArgumentCaptor<ScoreBoard> scoreBoardArgumentCaptor = ArgumentCaptor.forClass(ScoreBoard.class);
        verify(repository, times(1)).save(scoreBoardArgumentCaptor.capture());
    }

    @Test
    void createScoreBoard_shouldThrowException_whenScoreBoardHasNotEnded() {
        // Arrange
        ScoreBoard scoreBoard = getScoreBoard();

        when(repository.findByHomeTeamNameAndAwayTeamNameAndDeletedIsFalse(scoreBoard.getHomeTeamName(), scoreBoard.getAwayTeamName())).thenReturn(Optional.of(scoreBoard));

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> service.createScoreBoard(scoreBoard));

        // Assertions
        assertEquals(LiveDataErrorCode.SCORE_BOARD_HAS_NOT_ENDED, exception.getErrorCode());
    }

    @Test
    void update_shouldThrowException_whenScoreBoardIsNotFound() {
        // Arrange
        Long scoreBoardId = new Random().nextLong();

        ScoreBoard scoreBoard = new ScoreBoard();
        scoreBoard.setId(scoreBoardId);

        when(repository.findByIdAndDeletedIsFalse(scoreBoardId)).thenReturn(Optional.empty());

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> service.updateScoreBoardById(scoreBoardId, scoreBoard));

        // Assertions
        assertEquals(LiveDataErrorCode.SCORE_BOARD_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void update_shouldUpdateScoreBoard_whenScoreBoardIsFound() {
        // Arrange
        Long scoreBoardId = new Random().nextLong();

        ScoreBoard scoreBoard = getScoreBoardForUpdate();

        when(repository.findByIdAndDeletedIsFalse(scoreBoardId)).thenReturn(Optional.of(new ScoreBoard()));

        // Act
        service.updateScoreBoardById(scoreBoardId, scoreBoard);

        // Assertions
        ArgumentCaptor<ScoreBoard> scoreBoardArgumentCaptor = ArgumentCaptor.forClass(ScoreBoard.class);
        verify(repository).save(scoreBoardArgumentCaptor.capture());

        ScoreBoard scoreBoardArgumentCaptorValue = scoreBoardArgumentCaptor.getValue();

        assertEquals("TURKEY", scoreBoardArgumentCaptorValue.getHomeTeamName());
        assertEquals("IRELAND", scoreBoardArgumentCaptorValue.getAwayTeamName());
        assertEquals(0, scoreBoardArgumentCaptorValue.getHomeTeamScore());
        assertEquals(1, scoreBoardArgumentCaptorValue.getAwayTeamScore());
    }

    @Test
    void getAllScoreBoards() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        // Act
        service.getAllScoreBoards(pageable);

        // Assertions
        verify(repository, times(1)).findAllByDeletedIsFalse(pageable);
    }

    @Test
    void findById_shouldThrowException_whenScoreBoardDoesNotExistWithId() {
        // Arrange
        Long scoreBoardId = new Random().nextLong();

        when(repository.findByIdAndDeletedIsFalse(scoreBoardId)).thenReturn(Optional.empty());

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> service.getScoreBoardById(scoreBoardId));

        // Assertions
        assertEquals(LiveDataErrorCode.SCORE_BOARD_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getScoreBoardById() {
        // Arrange
        Long scoreBoardId = new Random().nextLong();

        ScoreBoard scoreBoard = new ScoreBoard();
        scoreBoard.setId(scoreBoardId);

        when(repository.findByIdAndDeletedIsFalse(scoreBoardId)).thenReturn(Optional.of(scoreBoard));

        // Act
        ScoreBoard objectUnderTest = service.getScoreBoardById(scoreBoardId);

        // Assertions
        assertEquals(objectUnderTest.getId(), scoreBoardId);
    }

    @Test
    void delete_shouldThrowException_whenScoreBoardDoesNotExistWithId() {
        Long scoreBoardId = new Random().nextLong();

        when(repository.findByIdAndDeletedIsFalse(scoreBoardId)).thenReturn(Optional.empty());

        // Act
        BaseException exception = assertThrows(BaseException.class, () -> service.getScoreBoardById(scoreBoardId));

        // Assertions
        assertEquals(LiveDataErrorCode.SCORE_BOARD_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void delete_shouldUpdateDeletedAsTrue_whenScoreBoardExists() {
        Long scoreBoardId = new Random().nextLong();

        ScoreBoard scoreBoard = new ScoreBoard();
        scoreBoard.setId(scoreBoardId);

        when(repository.findByIdAndDeletedIsFalse(scoreBoardId)).thenReturn(Optional.of(scoreBoard));

        // Act
        service.removeScoreBoardById(scoreBoardId);

        // Assertions
        verify(repository).delete(any(ScoreBoard.class));
    }

    @Test
    void findAll_shouldReturnListOfString_whenGetAllScoreBoardsInAscendingOrderByModifiedDateTimeIsCalled() {
        List<ScoreBoard> scoreBoardList = getScoreBoardList();
        List<String> scoreBoardsAsStringList = getScoreBoardsAsStringList();

        when(repository.findAllByDeletedIsTrue(Sort.by("modifiedDateTime").ascending())).thenReturn(scoreBoardList);

        // Act
        List<String> allScoreBoardsInAscendingOrderByModifiedDateTime = service.getAllScoreBoardsInAscendingOrderByModifiedDateTime();

        // Assertions
        assertEquals(scoreBoardList.size(), allScoreBoardsInAscendingOrderByModifiedDateTime.size());
        assertEquals(scoreBoardsAsStringList.get(0), allScoreBoardsInAscendingOrderByModifiedDateTime.get(0));
        assertEquals(scoreBoardsAsStringList.get(1), allScoreBoardsInAscendingOrderByModifiedDateTime.get(1));
        assertEquals(scoreBoardsAsStringList.get(2), allScoreBoardsInAscendingOrderByModifiedDateTime.get(2));
        assertEquals(scoreBoardsAsStringList.get(3), allScoreBoardsInAscendingOrderByModifiedDateTime.get(3));
        assertEquals(scoreBoardsAsStringList.get(4), allScoreBoardsInAscendingOrderByModifiedDateTime.get(4));
    }
}