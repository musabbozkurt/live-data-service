package com.mb.livedataservice.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for retrieving localized messages.
 * Can be used in both Spring-managed beans and static contexts (like tests).
 */
@Component
public final class MessageUtils {

    private static final String BUNDLE_NAME = "messages";
    private static MessageSource messageSource;

    /**
     * Constructor used by Spring to inject MessageSource.
     *
     * @param messageSource the message source to use
     */
    MessageUtils(MessageSource messageSource) {
        initialize(messageSource);
    }

    private static synchronized void initialize(MessageSource source) {
        messageSource = source;
    }

    /**
     * Get message using Spring's MessageSource (for use in Spring-managed beans).
     * Uses the current locale from LocaleContextHolder.
     *
     * @param key the message key
     * @return the localized message
     */
    public static String getMessage(String key) {
        return getMessage(key, LocaleContextHolder.getLocale());
    }

    /**
     * Get message using Spring's MessageSource with specific locale.
     *
     * @param key    the message key
     * @param locale the locale to use
     * @return the localized message
     */
    public static String getMessage(String key, Locale locale) {
        if (messageSource != null) {
            return messageSource.getMessage(key, null, key, locale);
        }
        // Fallback to ResourceBundle for static/test contexts
        return getMessageFromBundle(key, locale);
    }

    /**
     * Get message using ResourceBundle (for use in static contexts like tests).
     *
     * @param key    the message key
     * @param locale the locale to use
     * @return the localized message
     */
    public static String getMessageFromBundle(String key, Locale locale) {
        try {
            return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
        } catch (MissingResourceException _) {
            // Fallback to default locale if specific locale not found
            try {
                return ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH).getString(key);
            } catch (MissingResourceException _) {
                return key;
            }
        }
    }
}
