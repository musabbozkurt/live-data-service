package com.mb.livedataservice.api.controller;

import com.mb.livedataservice.api.request.ApiQueueRequest;
import com.mb.livedataservice.queue.producer.ProducerService;
import com.mb.livedataservice.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(value = "Queue Controller")
public class QueueController {

    private final ProducerService producerService;

    @PostMapping("/messages")
    @ApiOperation(value = "Publish a message")
    public void publishMessage(@RequestBody @Valid ApiQueueRequest apiQueueRequest) {
        log.info("Received a request to publish a message. publishMessage - ApiQueueRequest: {}.", apiQueueRequest);
        producerService.publishMessage(JsonUtils.serialize(apiQueueRequest));
    }

}
