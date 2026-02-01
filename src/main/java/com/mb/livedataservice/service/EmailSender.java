package com.mb.livedataservice.service;

import com.mb.livedataservice.queue.dto.EmailEventDto;

public interface EmailSender {

    void send(EmailEventDto emailEventDto);
}
