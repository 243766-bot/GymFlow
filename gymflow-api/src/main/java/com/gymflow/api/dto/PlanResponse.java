package com.gymflow.api.dto;

import com.gymflow.api.entity.Plan;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Información de un plan de membresía")
public record PlanResponse(
        @Schema(description = "ID único del plan", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Nombre del plan", example = "Mensualidad Estándar", requiredMode = Schema.RequiredMode.REQUIRED)
        String nombre,

        @Schema(description = "Duración en días", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
        int duracionDias,

        @Schema(description = "Precio en MXN", example = "450.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal precio
) {
    public static PlanResponse from(Plan p) {
        return new PlanResponse(p.getId(), p.getNombre(), p.getDuracionDias(), p.getPrecio());
    }
}