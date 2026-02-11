package com.mb.livedataservice.queue.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents an email attachment.
 * Supports various file types including PDF, Excel, CSV, TXT, etc.
 */
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {

    /**
     * The name of the attachment file (e.g., "report.pdf", "data.xlsx")
     */
    private String fileName;

    /**
     * The content of the attachment as a byte array
     */
    private byte[] content;

    /**
     * The MIME type of the attachment (e.g., "application/pdf", "text/csv")
     * If not provided, it will be auto-detected based on the file extension
     */
    private String contentType;
}
