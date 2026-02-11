package com.mb.livedataservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.net.URLConnection;

/**
 * Utility class for resolving MIME types.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MimeTypeUtils {

    /**
     * Resolves the content type for a file based on its name.
     * Falls back to "application/octet-stream" if unable to determine.
     *
     * @param fileName the name of the file
     * @return the resolved MIME type
     */
    public static String resolveContentType(String fileName) {
        return resolveContentType(fileName, null);
    }

    /**
     * Resolves the content type for a file.
     * If an explicit content type is provided, it is used.
     * Otherwise, the content type is guessed from the file name.
     * Falls back to "application/octet-stream" if unable to determine.
     *
     * @param fileName    the name of the file
     * @param contentType the explicit content type (can be null)
     * @return the resolved MIME type
     */
    public static String resolveContentType(String fileName, String contentType) {
        // Use explicit content type if provided
        if (StringUtils.isNotBlank(contentType)) {
            return contentType;
        }

        // Try to guess from file name
        if (StringUtils.isNotBlank(fileName)) {
            String guessedType = URLConnection.guessContentTypeFromName(fileName);
            if (guessedType != null) {
                return guessedType;
            }

            // Handle common file types that URLConnection might not recognize
            String lowerFileName = fileName.toLowerCase();
            if (lowerFileName.endsWith(".pdf")) {
                return "application/pdf";
            } else if (lowerFileName.endsWith(".xlsx")) {
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else if (lowerFileName.endsWith(".xls")) {
                return "application/vnd.ms-excel";
            } else if (lowerFileName.endsWith(".csv")) {
                return "text/csv";
            } else if (lowerFileName.endsWith(".txt")) {
                return "text/plain";
            } else if (lowerFileName.endsWith(".docx")) {
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            } else if (lowerFileName.endsWith(".doc")) {
                return "application/msword";
            } else if (lowerFileName.endsWith(".png")) {
                return "image/png";
            } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (lowerFileName.endsWith(".gif")) {
                return "image/gif";
            } else if (lowerFileName.endsWith(".zip")) {
                return "application/zip";
            } else if (lowerFileName.endsWith(".json")) {
                return "application/json";
            } else if (lowerFileName.endsWith(".xml")) {
                return "application/xml";
            }
        }

        // Default fallback
        return "application/octet-stream";
    }
}
