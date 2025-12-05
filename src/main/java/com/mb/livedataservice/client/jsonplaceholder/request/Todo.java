package com.mb.livedataservice.client.jsonplaceholder.request;

import jakarta.validation.constraints.NotBlank;

public record Todo(Integer id,
                   Integer userId,
                   @NotBlank(message = "Title is required") String title,
                   boolean completed) {

}
