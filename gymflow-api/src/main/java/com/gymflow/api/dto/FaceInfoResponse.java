package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Estado del registro biométrico facial de un usuario")
public record FaceInfoResponse(
        @Schema(description = "Indica si el cliente tiene plantilla facial registrada", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean registrado,

        @Schema(description = "Fecha y hora de registro biométrico (null si no está registrado)", example = "2026-11-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDateTime fechaRegistro,

        @Schema(description = "Miniatura JPEG en Base64 si fue almacenada", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String foto
) {}