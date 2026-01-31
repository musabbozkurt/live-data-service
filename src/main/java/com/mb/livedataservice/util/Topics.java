package com.mb.livedataservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Topics {

    public static final String TEST_TOPIC = "test-topic";
    public static final String ORDERS = "orders";
    public static final String CUSTOM_ORDERS = "custom-orders";
    public static final String JMS_CUSTOM_ORDERS = "jms-custom-orders";
    public static final String EMAIL_TOPIC = "email-topic";
}
