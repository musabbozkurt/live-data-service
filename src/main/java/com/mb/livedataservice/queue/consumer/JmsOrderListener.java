package com.mb.livedataservice.queue.consumer;

import com.mb.livedataservice.queue.dto.Order;
import com.mb.livedataservice.util.Topics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JmsOrderListener {

    @JmsListener(destination = Topics.JMS_CUSTOM_ORDERS)
    public void receiveOrder(Order order) {
        log.info("Received an order from JMS. receiveOrder - Order: {}.", order);
    }
}
