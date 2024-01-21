package com.mb.livedataservice.service;

import com.mb.livedataservice.data.model.ScoreBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ScoreBoardService {

    ScoreBoard createScoreBoard(ScoreBoard scoreBoard);

    Page<ScoreBoard> getAllScoreBoards(Pageable pageable);

    ScoreBoard getScoreBoardById(Long id);

    ScoreBoard updateScoreBoardById(Long id, ScoreBoard scoreBoard);

    void removeScoreBoardById(Long id);

    List<String> getAllScoreBoardsInAscendingOrderByModifiedDateTime();
}