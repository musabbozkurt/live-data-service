package com.mb.livedataservice.util;

import com.mb.livedataservice.queue.dto.EmailEventDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmailUtils {

    public static boolean isValid(EmailEventDto emailEventDto) {
        return (emailEventDto.hasTemplate()
                || (StringUtils.hasText(emailEventDto.getSubject()) && StringUtils.hasText(emailEventDto.getBody())))
                && isValidEmailCollection(emailEventDto.getTo(), true)
                && isValidEmailCollection(emailEventDto.getCc(), false)
                && isValidEmailCollection(emailEventDto.getBcc(), false);
    }

    private static boolean isValidEmailCollection(Set<String> emails, boolean required) {
        return CollectionUtils.isEmpty(emails) ? !required : emails.parallelStream().allMatch(EmailValidator.getInstance()::isValid);
    }
}
