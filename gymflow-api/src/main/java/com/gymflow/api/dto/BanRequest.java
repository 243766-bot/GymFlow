package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud para suspender o reactivar el acceso de un usuario")
public record BanRequest(
        @Schema(description = "true para banear/suspender acceso; false para reactivar", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Indica si el usuario queda baneado (true) o reactivado (false)")
        Boolean baneado
) {}