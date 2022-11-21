package com.sportradar.livedataservice.api.controller;

import com.sportradar.livedataservice.api.request.ApiScoreBoardRequest;
import com.sportradar.livedataservice.api.request.ApiScoreBoardUpdateRequest;
import com.sportradar.livedataservice.api.response.ApiScoreBoardResponse;
import com.sportradar.livedataservice.mapper.ScoreBoardMapper;
import com.sportradar.livedataservice.service.ScoreBoardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(value = "Score Board Controller")
public class ScoreBoardController {

    private final ScoreBoardService scoreBoardService;
    private final ScoreBoardMapper scoreBoardMapper;

    @PostMapping("/score-boards")
    @ApiOperation(value = "Create score board")
    public ResponseEntity<ApiScoreBoardResponse> createScoreBoard(@RequestBody @Valid ApiScoreBoardRequest apiScoreBoardRequest) {
        log.info("Received a request to create score board. createScoreBoard - ApiScoreBoardRequest: {}.", apiScoreBoardRequest);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.createScoreBoard(scoreBoardMapper.map(apiScoreBoardRequest))));
    }

    @GetMapping("/score-boards")
    @ApiOperation(value = "Get all score board by pagination")
    public ResponseEntity<Page<ApiScoreBoardResponse>> getAllScoreBoards(Pageable pageable) {
        log.info("Received a request to get all score board by pagination. getAllScoreBoards - Pageable: {}", pageable);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.getAllScoreBoards(pageable)));
    }

    @GetMapping("/score-boards/{id}")
    @ApiOperation(value = "Get score board by id")
    public ResponseEntity<ApiScoreBoardResponse> getScoreBoardById(@PathVariable Long id) {
        log.info("Received a request to get score board by id. getScoreBoardById - Id: {}", id);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.getScoreBoardById(id)));
    }

    @PutMapping("/score-boards/{id}")
    @ApiOperation(value = "Update score board by id")
    public ResponseEntity<ApiScoreBoardResponse> updateScoreBoardById(@PathVariable Long id,
                                                                      @RequestBody @Valid ApiScoreBoardUpdateRequest apiScoreBoardUpdateRequest) {
        log.info("Received a request to update score board by id. updateScoreBoardById - Id: {}, ApiScoreBoardUpdateRequest: {}", id, apiScoreBoardUpdateRequest);
        return ResponseEntity.ok(scoreBoardMapper.map(scoreBoardService.updateScoreBoardById(id, scoreBoardMapper.map(apiScoreBoardUpdateRequest))));
    }

    @DeleteMapping("/score-boards/{id}")
    @ApiOperation(value = "Remove score board by id")
    public void removeScoreBoardById(@PathVariable Long id) {
        log.info("Received a request to remove score board by id. removeScoreBoardById - Id: {}.", id);
        scoreBoardService.removeScoreBoardById(id);
    }

    @GetMapping("/score-boards/summary")
    @ApiOperation(value = "Get all score boards in ascending order by modified date time")
    public ResponseEntity<List<String>> getAllScoreBoardsInAscendingOrderByModifiedDateTime() {
        log.info("Received a request to get all score boards in ascending order by modified date time. getAllScoreBoardsInAscendingOrderByModifiedDateTime.");
        return ResponseEntity.ok(scoreBoardService.getAllScoreBoardsInAscendingOrderByModifiedDateTime());
    }

}
