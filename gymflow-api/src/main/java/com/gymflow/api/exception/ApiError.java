package com.gymflow.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Estructura estandarizada para respuestas de error HTTP (4xx y 5xx)")
public record ApiError(
        @Schema(description = "Fecha y hora en que ocurrió el error", example = "2026-11-20T13:15:30", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime timestamp,

        @Schema(description = "Código de estado HTTP", example = "400", requiredMode = Schema.RequiredMode.REQUIRED)
        int status,

        @Schema(description = "Nombre estándar del estado HTTP", example = "Bad Request", requiredMode = Schema.RequiredMode.REQUIRED)
        String error,

        @Schema(description = "Detalle legible de la causa del error", example = "El correo ya está registrado", requiredMode = Schema.RequiredMode.REQUIRED)
        String message
) {
}