package com.mb.livedataservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApiPlayIntegrityTokenResult {

    @Schema(description = "Play integrity token")
    private String token;

    // This is an optional field.
    @Schema(description = "Package name which is mobile client package name", example = "com.mb.android")
    private String packageName;

    public String getPackageName() {
        return this.packageName == null ? null : "com.mb.android";
    }
}
