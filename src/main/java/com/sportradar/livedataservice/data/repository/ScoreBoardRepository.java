package com.sportradar.livedataservice.data.repository;

import com.sportradar.livedataservice.data.model.ScoreBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreBoardRepository extends JpaRepository<ScoreBoard, Long> {

    Optional<ScoreBoard> findByHomeTeamNameAndAwayTeamNameAndDeletedIsFalse(String homeTeamName, String awayTeamName);

    Page<ScoreBoard> findAllByDeletedIsFalse(Pageable pageable);

    Optional<ScoreBoard> findByIdAndDeletedIsFalse(Long id);

    List<ScoreBoard> findAllByDeletedIsTrue(Sort sort);

}