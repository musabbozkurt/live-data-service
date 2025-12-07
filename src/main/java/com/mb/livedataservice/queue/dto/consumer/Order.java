package com.mb.livedataservice.queue.dto.consumer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record Order(@NotNull UUID orderId, @NotNull UUID articleId, @Positive int amount) {

}
