package com.gymflow.api.dto;

import com.gymflow.api.entity.EstadoMembresia;
import com.gymflow.api.entity.Membresia;
import com.gymflow.api.util.Fechas;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Schema(description = "Detalle de membresía con estado calculado en tiempo real y días restantes")
public record MembresiaResponse(
        @Schema(description = "ID único de la membresía", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "ID del cliente titular", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Long usuarioId,

        @Schema(description = "Nombre del cliente titular", example = "Carlos Mendoza", requiredMode = Schema.RequiredMode.REQUIRED)
        String usuarioNombre,

        @Schema(description = "Plan contratado", requiredMode = Schema.RequiredMode.REQUIRED)
        PlanResponse plan,

        @Schema(description = "Fecha de inicio de vigencia", example = "2026-11-01", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate fechaInicio,

        @Schema(description = "Fecha de vencimiento", example = "2026-12-01", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate fechaVencimiento,

        @Schema(description = "Estado calculado de la membresía", example = "ACTIVA", allowableValues = {"ACTIVA", "VENCIDA", "CANCELADA"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String estado,

        @Schema(description = "Días restantes de vigencia (0 si está vencida o cancelada)", example = "11", requiredMode = Schema.RequiredMode.REQUIRED)
        long diasRestantes
) {
    public static MembresiaResponse from(Membresia m) {
        LocalDate hoy = Fechas.hoy();
        String estado;
        if (m.getEstado() == EstadoMembresia.CANCELADA) {
            estado = "CANCELADA";
        } else if (hoy.isAfter(m.getFechaVencimiento())) {
            estado = "VENCIDA";
        } else {
            estado = "ACTIVA";
        }
        long dias = estado.equals("ACTIVA") ? ChronoUnit.DAYS.between(hoy, m.getFechaVencimiento()) : 0;
        return new MembresiaResponse(
                m.getId(), m.getUsuario().getId(), m.getUsuario().getNombre(),
                PlanResponse.from(m.getPlan()), m.getFechaInicio(), m.getFechaVencimiento(),
                estado, dias
        );
    }
}