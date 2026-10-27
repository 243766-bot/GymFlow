package com.gymflow.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FaceRegisterRequest(
        @NotNull(message = "El descriptor es obligatorio")
        @Size(min = 128, max = 128, message = "El descriptor debe tener exactamente 128 valores")
        List<@NotNull Double> descriptor,

        @NotNull(message = "Debes indicar si el cliente dio su consentimiento")
        Boolean consentimiento
) {
}