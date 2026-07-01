package com.mb.livedataservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomStringUtils {

    public static String toCamelCase(String key) {
        StringBuilder result = new StringBuilder();
        boolean upperNext = false;
        for (char character : key.toCharArray()) {
            if (character == '-' || character == '_') {
                upperNext = true;
                continue;
            }
            result.append(upperNext ? Character.toUpperCase(character) : character);
            upperNext = false;
        }
        return result.toString();
    }
}
