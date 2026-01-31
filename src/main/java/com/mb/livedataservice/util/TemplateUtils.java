package com.mb.livedataservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TemplateUtils {

    /**
     * Resolves placeholders in the given text with values from the parameters map.
     * Placeholders are expected in the format {{parameterName}}.
     *
     * @param text       the text containing placeholders
     * @param parameters the map of parameter names to values
     * @return the text with placeholders replaced by their values
     */
    public static String resolvePlaceholders(String text, Map<String, String> parameters) {
        if (text == null || parameters == null || parameters.isEmpty()) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue() != null ? entry.getValue() : "");
        }
        return result;
    }
}
