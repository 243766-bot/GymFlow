package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta estándar de confirmación de operación")
public record MensajeResponse(
        @Schema(description = "Mensaje descriptivo del resultado", example = "Operación realizada correctamente", requiredMode = Schema.RequiredMode.REQUIRED)
        String mensaje
) {}