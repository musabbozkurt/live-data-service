package com.mb.livedataservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Email attachment request. Supports various file types including PDF, Excel, CSV, TXT, images, etc.",
        example = """
                {
                  "fileName": "report.pdf",
                  "contentBase64": "JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4=",
                  "contentType": "application/pdf"
                }
                """
)
public class EmailAttachmentRequest {

    @NotBlank(message = "{validation.attachment.fileName.notBlank}")
    @Size(max = 255, message = "{validation.attachment.fileName.size}")
    @Schema(description = "The name of the attachment file (e.g., report.pdf, logo.png, data.xlsx, export.csv)", example = "report.pdf")
    private String fileName;

    @NotNull(message = "{validation.attachment.content.notNull}")
    @Schema(description = """
            The content of the attachment as a Base64 encoded string.
            Generate using command: base64 -i <file>
            
            Examples:
            - PDF: JVBERi0xLjQKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4=
            - PNG Image (1x1 pixel): iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==
            - CSV: TmFtZSxBZ2UsSW5jb21lCkpvaG4sMzAsNTAwMDAKSmFuZSwyNSw2MDAwMA==
            """,
            example = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==")
    private String contentBase64;

    @Size(max = 100, message = "{validation.attachment.contentType.size}")
    @Schema(description = """
            The MIME type of the attachment. If not provided, it will be auto-detected based on the file extension.
            
            Common types:
            - PDF: application/pdf
            - PNG Image: image/png
            - JPEG Image: image/jpeg
            - Excel: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
            - CSV: text/csv
            - Text: text/plain
            """,
            example = "image/png")
    private String contentType;
}
