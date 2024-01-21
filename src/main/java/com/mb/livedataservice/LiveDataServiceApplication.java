package com.mb.livedataservice;

import com.mb.livedataservice.data.model.ScoreBoard;
import com.mb.livedataservice.data.repository.ScoreBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.OffsetDateTime;
import java.util.Arrays;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class LiveDataServiceApplication implements CommandLineRunner {

    private final ScoreBoardRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(LiveDataServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            ScoreBoard scoreBoard = new ScoreBoard("Uruguay", "Italy", 6, 6);
            scoreBoard.setModifiedDateTime(OffsetDateTime.now().minusHours(1));
            scoreBoard.setDeleted(true);
            ScoreBoard scoreBoard1 = new ScoreBoard("Spain", "Brazil", 10, 2);
            scoreBoard1.setModifiedDateTime(OffsetDateTime.now().minusHours(2));
            scoreBoard1.setDeleted(true);
            ScoreBoard scoreBoard2 = new ScoreBoard("Mexico", "Canada", 0, 5);
            scoreBoard2.setModifiedDateTime(OffsetDateTime.now().minusHours(3));
            scoreBoard2.setDeleted(true);
            ScoreBoard scoreBoard3 = new ScoreBoard("Argentina", "Australia", 3, 1);
            scoreBoard3.setModifiedDateTime(OffsetDateTime.now().minusHours(4));
            scoreBoard3.setDeleted(true);
            ScoreBoard scoreBoard4 = new ScoreBoard("Germany", "France", 2, 2);
            scoreBoard4.setModifiedDateTime(OffsetDateTime.now().minusHours(5));
            scoreBoard4.setDeleted(true);
            repository.saveAll(Arrays.asList(scoreBoard, scoreBoard1, scoreBoard2, scoreBoard3, scoreBoard4));
        } catch (Exception e) {
            log.error("Exception occurred while saving entities. run - Exception: {}", ExceptionUtils.getStackTrace(e));
        }
    }
}
