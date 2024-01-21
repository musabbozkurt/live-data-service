package com.mb.livedataservice.service.impl;

import com.mb.livedataservice.data.model.ScoreBoard;
import com.mb.livedataservice.data.repository.ScoreBoardRepository;
import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import com.mb.livedataservice.service.ScoreBoardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreBoardServiceImpl implements ScoreBoardService {

    private final ScoreBoardRepository scoreBoardRepository;

    @Override
    public ScoreBoard createScoreBoard(ScoreBoard scoreBoard) {
        Optional<ScoreBoard> optionalScoreBoard = scoreBoardRepository.findByHomeTeamNameAndAwayTeamNameAndDeletedIsFalse(scoreBoard.getHomeTeamName(), scoreBoard.getAwayTeamName());
        if (optionalScoreBoard.isPresent()) {
            throw new BaseException(LiveDataErrorCode.SCORE_BOARD_HAS_NOT_ENDED);
        }
        return scoreBoardRepository.save(scoreBoard);
    }

    @Override
    public Page<ScoreBoard> getAllScoreBoards(Pageable pageable) {
        return scoreBoardRepository.findAllByDeletedIsFalse(pageable);
    }

    @Override
    public ScoreBoard getScoreBoardById(Long id) {
        Optional<ScoreBoard> optionalScoreBoard = scoreBoardRepository.findByIdAndDeletedIsFalse(id);
        if (optionalScoreBoard.isPresent()) {
            return optionalScoreBoard.get();
        } else {
            throw new BaseException(LiveDataErrorCode.SCORE_BOARD_NOT_FOUND);
        }
    }

    @Override
    public ScoreBoard updateScoreBoardById(Long id, ScoreBoard scoreBoard) {
        ScoreBoard scoreBoardById = getScoreBoardById(id);
        scoreBoardById.setHomeTeamName(scoreBoard.getHomeTeamName());
        scoreBoardById.setAwayTeamName(scoreBoard.getAwayTeamName());
        scoreBoardById.setHomeTeamScore(scoreBoard.getHomeTeamScore());
        scoreBoardById.setAwayTeamScore(scoreBoard.getAwayTeamScore());
        return scoreBoardRepository.save(scoreBoardById);
    }

    @Override
    public void removeScoreBoardById(Long id) {
        scoreBoardRepository.delete(getScoreBoardById(id));
    }

    @Override
    public List<String> getAllScoreBoardsInAscendingOrderByModifiedDateTime() {
        return scoreBoardRepository.findAllByDeletedIsTrue(Sort.by("modifiedDateTime").ascending())
                .stream()
                .map(scoreBoard -> String.format("%d. %s - %s : %d - %d", scoreBoard.getId(), scoreBoard.getHomeTeamName(), scoreBoard.getAwayTeamName(), scoreBoard.getHomeTeamScore(), scoreBoard.getAwayTeamScore()))
                .collect(Collectors.toList());
    }
}