package com.gymflow.api.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull(message = "El plan es obligatorio")
        Long planId
) {
}