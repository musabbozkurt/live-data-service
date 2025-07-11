package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.ApiScoreBoardRequest;
import com.mb.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.mb.livedataservice.api.response.ApiScoreBoardResponse;
import com.mb.livedataservice.mapper.ScoreBoardMapper;
import com.mb.livedataservice.service.ScoreBoardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ScoreBoardController {

    private final ScoreBoardService scoreBoardService;
    private final ScoreBoardMapper scoreBoardMapper;

    @PostMapping("/score-boards")
    @Operation(summary = "Create score board")
    public ResponseEntity<ApiScoreBoardResponse> createScoreBoard(@RequestBody @Valid ApiScoreBoardRequest apiScoreBoardRequest) {
        log.info("Received a request to create score board. createScoreBoard - ApiScoreBoardRequest: {}.", apiScoreBoardRequest);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.createScoreBoard(scoreBoardMapper.map(apiScoreBoardRequest))));
    }

    @GetMapping("/score-boards")
    @Operation(summary = "Get all score board by pagination")
    public ResponseEntity<Page<ApiScoreBoardResponse>> getAllScoreBoards(Pageable pageable) {
        log.info("Received a request to get all score board by pagination. getAllScoreBoards - Pageable: {}", pageable);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.getAllScoreBoards(pageable)));
    }

    @GetMapping("/score-boards/{id}")
    @Operation(summary = "Get score board by id")
    public ResponseEntity<ApiScoreBoardResponse> getScoreBoardById(@PathVariable Long id) {
        log.info("Received a request to get score board by id. getScoreBoardById - Id: {}", id);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.getScoreBoardById(id)));
    }

    @PutMapping("/score-boards/{id}")
    @Operation(summary = "Update score board by id")
    public ResponseEntity<ApiScoreBoardResponse> updateScoreBoardById(@PathVariable Long id,
                                                                      @RequestBody @Valid ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest) {
        log.info("Received a request to update score board by id. updateScoreBoardById - Id: {}, ApiScoreBoardUpdateRequest: {}", id, apiScoreBoardUpdateRequest);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.updateScoreBoardById(id, scoreBoardMapper.map(apiScoreBoardUpdateRequest))));
    }

    @DeleteMapping("/score-boards/{id}")
    @Operation(summary = "Remove score board by id")
    public void removeScoreBoardById(@PathVariable Long id) {
        log.info("Received a request to remove score board by id. removeScoreBoardById - Id: {}.", id);
        scoreBoardService.removeScoreBoardById(id);
    }

    @GetMapping("/score-boards/summary")
    @Operation(summary = "Get all score boards in ascending order by modified date time")
    public ResponseEntity<List<String>> getAllScoreBoardsInAscendingOrderByModifiedDateTime() {
        log.info("Received a request to get all score boards in ascending order by modified date time. getAllScoreBoardsInAscendingOrderByModifiedDateTime.");
        return ResponseEntity.ok(scoreBoardService.getAllScoreBoardsInAscendingOrderByModifiedDateTime());
    }
}
