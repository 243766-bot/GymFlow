package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Solicitud de autenticación biométrica mediante vector facial")
public record FaceLoginRequest(
        @Schema(description = "Desafío vigente obtenido en /api/v1/auth/face/challenge", example = "f8a9c2e1-4b7d-4e9a-9c3b-1a2b3c4d5e6f", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El desafío es obligatorio")
        String challenge,

        @Schema(description = "Vector descriptor facial de exactamente 128 valores numéricos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El descriptor es obligatorio")
        @Size(min = 128, max = 128, message = "El descriptor debe tener exactamente 128 valores")
        List<@NotNull Double> descriptor
) {}