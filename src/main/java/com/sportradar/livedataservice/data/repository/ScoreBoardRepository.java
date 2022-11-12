package com.sportradar.livedataservice.data.repository;

import com.sportradar.livedataservice.data.model.ScoreBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoreBoardRepository extends JpaRepository<ScoreBoard, Long> {

    Optional<ScoreBoard> findByHomeTeamNameAndAwayTeamName(String homeTeamName, String awayTeamName);

}