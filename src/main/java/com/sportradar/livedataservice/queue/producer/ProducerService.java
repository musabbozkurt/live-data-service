package com.sportradar.livedataservice.queue.producer;

public interface ProducerService {

    void publishMessage(String message);

}