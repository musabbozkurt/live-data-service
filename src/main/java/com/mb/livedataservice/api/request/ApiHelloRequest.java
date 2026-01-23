package com.mb.livedataservice.api.request;

import jakarta.validation.constraints.NotBlank;

public record ApiHelloRequest(@NotBlank(message = "Name is required") String name,
                              String greeting) {
}
