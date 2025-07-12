package com.mb.livedataservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KafkaTopics {

    public static final String TEST_TOPIC = "test-topic";
    public static final String ORDERS = "orders";
}
