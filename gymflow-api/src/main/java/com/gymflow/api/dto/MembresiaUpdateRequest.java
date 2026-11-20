package com.gymflow.api.dto;

import com.gymflow.api.entity.EstadoMembresia;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Campos editables de una membresía existente (todos opcionales)")
public record MembresiaUpdateRequest(
        @Schema(description = "Nuevo ID de plan", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long planId,

        @Schema(description = "Nueva fecha de inicio", example = "2026-11-20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDate fechaInicio,

        @Schema(description = "Nueva fecha de vencimiento", example = "2026-12-20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDate fechaVencimiento,

        @Schema(description = "Estado administrativo de la membresía", example = "ACTIVA", allowableValues = {"ACTIVA", "CANCELADA"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        EstadoMembresia estado
) {}