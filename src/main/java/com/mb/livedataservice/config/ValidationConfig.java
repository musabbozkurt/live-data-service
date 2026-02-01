package com.mb.livedataservice.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * Configuration for validation messages and locale resolution.
 * Supports internationalization (i18n) for validation error messages.
 */
@Configuration
public class ValidationConfig {

    /**
     * Configures the message source for loading validation messages from properties files.
     * Automatically resolves messages from {@code messages.properties} and locale-specific
     * variants (e.g., {@code messages_tr.properties} for Turkish).
     *
     * @return the configured message source
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);
        return messageSource;
    }

    /**
     * Configures the validator factory to use the message source for validation messages.
     * Enables localized validation error messages based on the current locale.
     *
     * @param messageSource the message source to use for validation messages
     * @return the configured validator factory bean
     */
    @Bean
    public LocalValidatorFactoryBean validator(MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    /**
     * Configures the locale resolver to use the {@code Accept-Language} HTTP header.
     * Defaults to English if no language header is provided.
     *
     * @return the configured locale resolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }
}
