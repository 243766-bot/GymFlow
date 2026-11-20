package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación exitosa con token JWT y datos del usuario")
public record AuthResponse(
        @Schema(description = "Token de acceso JWT", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
        String token,

        @Schema(description = "Esquema del token", example = "Bearer", requiredMode = Schema.RequiredMode.REQUIRED)
        String tipo,

        @Schema(description = "Información del usuario autenticado", requiredMode = Schema.RequiredMode.REQUIRED)
        UsuarioResponse usuario
) {}