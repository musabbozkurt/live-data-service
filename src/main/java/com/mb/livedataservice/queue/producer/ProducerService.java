package com.mb.livedataservice.queue.producer;

public interface ProducerService {

    void publishMessage(String message);

    void publishJmsMessage();
}