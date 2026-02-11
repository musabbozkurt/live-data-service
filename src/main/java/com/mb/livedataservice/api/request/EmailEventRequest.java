package com.mb.livedataservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Schema(description = "Email event request for sending emails with optional attachments")
public class EmailEventRequest {

    @Size(min = 1, max = 100, message = "{validation.subject.size}")
    @Schema(description = "Email subject", example = "Monthly Report - January 2026")
    private String subject;

    @NotEmpty(message = "{validation.body.notEmpty}")
    @Schema(description = "Email body content (plain text or HTML)", example = "Please find attached the monthly report.")
    private String body;

    @NotEmpty(message = "{validation.to.size}")
    @Schema(description = "List of recipient email addresses", example = "[\"john.doe@example.com\", \"jane.doe@example.com\"]")
    private Set<String> to = new HashSet<>();

    @Schema(description = "List of CC email addresses", example = "[\"manager@example.com\"]")
    private Set<String> cc = new HashSet<>();

    @Schema(description = "List of BCC email addresses", example = "[\"archive@example.com\"]")
    private Set<String> bcc = new HashSet<>();

    @Size(max = 100, message = "{validation.templateCode.size}")
    @Schema(description = "Template code for using predefined email templates", example = "WELCOME_EMAIL")
    private String templateCode;

    @Schema(description = "Parameters to be used in the email template",
            example = """
                    {
                        "userName": "John",
                        "activationLink": "https://example.com/activate"
                    }
                    """
    )
    private Map<String, Object> templateParameters = new HashMap<>();

    @Valid
    @Schema(description = "List of file attachments (PDF, Excel, CSV, TXT, etc.)")
    private List<EmailAttachmentRequest> attachments = new ArrayList<>();
}
