package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.EmailEventRequest;
import com.mb.livedataservice.mapper.EmailEventDtoMapper;
import com.mb.livedataservice.queue.producer.impl.EmailEventProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
public class EmailEventController {

    private final EmailEventProducer emailEventProducer;
    private final EmailEventDtoMapper emailEventDtoMapper;

    @PostMapping
    public ResponseEntity<Void> produce(@Valid @RequestBody EmailEventRequest request) {
        emailEventProducer.produce(emailEventDtoMapper.toDto(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
