package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.filter.ApiTutorialFilter;
import com.mb.livedataservice.api.request.ApiTutorialRequest;
import com.mb.livedataservice.api.request.ApiTutorialUpdateRequest;
import com.mb.livedataservice.api.response.ApiTutorialResponse;
import com.mb.livedataservice.mapper.TutorialMapper;
import com.mb.livedataservice.service.TutorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8081")
public class TutorialController {

    private final TutorialService tutorialService;
    private final TutorialMapper tutorialMapper;

    @PostMapping("/tutorials")
    public ResponseEntity<ApiTutorialResponse> createTutorial(@RequestBody ApiTutorialRequest apiTutorialRequest) {
        return new ResponseEntity<>(tutorialMapper.map(tutorialService.save(tutorialMapper.map(apiTutorialRequest))), HttpStatus.CREATED);
    }

    @GetMapping("/tutorials/{id}")
    public ResponseEntity<ApiTutorialResponse> getTutorialById(@PathVariable("id") long id) {
        return ResponseEntity.ok(tutorialMapper.map(tutorialService.findById(id)));
    }

    @GetMapping("/tutorials")
    public ResponseEntity<List<ApiTutorialResponse>> getAllTutorials(@RequestParam(required = false) String title) {
        return ResponseEntity.ok(tutorialMapper.map(tutorialService.findByTitleContaining(title)));
    }

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<ApiTutorialResponse> updateTutorial(@PathVariable("id") long id, @RequestBody ApiTutorialUpdateRequest apiTutorialUpdateRequest) {
        return new ResponseEntity<>(tutorialMapper.map(tutorialService.update(id, tutorialMapper.map(apiTutorialUpdateRequest))), HttpStatus.OK);
    }

    @DeleteMapping("/tutorials/{id}")
    public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
        tutorialService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/tutorials")
    public ResponseEntity<HttpStatus> deleteAllTutorials() {
        tutorialService.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/tutorials/published")
    public ResponseEntity<List<ApiTutorialResponse>> findByPublished() {
        return new ResponseEntity<>(tutorialMapper.map(tutorialService.findByPublished(true)), HttpStatus.OK);
    }

    @GetMapping("/tutorials/filter")
    public ResponseEntity<Page<ApiTutorialResponse>> findAll(ApiTutorialFilter apiTutorialFilter, Pageable pageable) {
        return new ResponseEntity<>(tutorialService.findAll(tutorialMapper.map(apiTutorialFilter), pageable).map(tutorialMapper::map), HttpStatus.OK);
    }
}
