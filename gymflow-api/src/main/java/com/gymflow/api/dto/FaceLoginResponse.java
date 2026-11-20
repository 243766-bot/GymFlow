package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de acceso por reconocimiento facial en recepción/caja")
public record FaceLoginResponse(
        @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        String token,

        @Schema(description = "Esquema del token", example = "Bearer", requiredMode = Schema.RequiredMode.REQUIRED)
        String tipo,

        @Schema(description = "Datos del usuario identificado", requiredMode = Schema.RequiredMode.REQUIRED)
        UsuarioResponse usuario,

        @Schema(description = "Membresía actual del cliente (null si no tiene)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        MembresiaResponse membresia
) {}