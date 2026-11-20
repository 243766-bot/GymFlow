package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Desafío criptográfico temporal para validar una captura facial en vivo")
public record FaceChallengeResponse(
        @Schema(description = "Cadena aleatoria de un solo uso", example = "f8a9c2e1-4b7d-4e9a-9c3b-1a2b3c4d5e6f", requiredMode = Schema.RequiredMode.REQUIRED)
        String challenge,

        @Schema(description = "Tiempo de vida del desafío en segundos", example = "60", requiredMode = Schema.RequiredMode.REQUIRED)
        long expiraEnSegundos
) {}