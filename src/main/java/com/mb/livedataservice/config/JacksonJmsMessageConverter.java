package com.mb.livedataservice.config;

import com.mb.livedataservice.exception.BaseException;
import com.mb.livedataservice.exception.LiveDataErrorCode;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class JacksonJmsMessageConverter implements MessageConverter {

    private final JsonMapper jsonMapper;

    public JacksonJmsMessageConverter() {
        this.jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Override
    public Message toMessage(Object object, Session session) {
        try {
            String json = jsonMapper.writeValueAsString(object);
            TextMessage message = session.createTextMessage(json);
            message.setStringProperty("_type", object.getClass().getName());
            return message;
        } catch (Exception e) {
            log.error("Exception occurred while converting to message. toMessage - Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException(LiveDataErrorCode.INVALID_VALUE);
        }
    }

    @Override
    public Object fromMessage(Message message) {
        if (message instanceof TextMessage textMessage) {
            try {
                String json = textMessage.getText();
                String className = message.getStringProperty("_type");
                Class<?> clazz = Class.forName(className);
                return jsonMapper.readValue(json, clazz);
            } catch (Exception e) {
                log.error("Exception occurred while converting from message. fromMessage - Exception: {}", ExceptionUtils.getStackTrace(e));
                throw new BaseException(LiveDataErrorCode.INVALID_VALUE);
            }
        }
        throw new BaseException(LiveDataErrorCode.INVALID_VALUE);
    }
}
