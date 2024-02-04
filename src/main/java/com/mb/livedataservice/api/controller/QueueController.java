package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.ApiQueueRequest;
import com.mb.livedataservice.queue.producer.ProducerService;
import com.mb.livedataservice.util.JsonUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueueController {

    private final ProducerService producerService;

    @PostMapping("/messages")
    @Operation(summary = "Publish a message")
    public void publishMessage(@RequestBody @Valid ApiQueueRequest apiQueueRequest) {
        log.info("Received a request to publish a message. publishMessage - ApiQueueRequest: {}.", apiQueueRequest);
        producerService.publishMessage(JsonUtils.serialize(apiQueueRequest));
    }
}
