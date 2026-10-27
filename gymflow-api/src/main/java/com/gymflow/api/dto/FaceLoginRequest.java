package com.gymflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FaceLoginRequest(
        @NotBlank(message = "El desafío es obligatorio")
        String challenge,

        @NotNull(message = "El descriptor es obligatorio")
        @Size(min = 128, max = 128, message = "El descriptor debe tener exactamente 128 valores")
        List<@NotNull Double> descriptor
) {
}