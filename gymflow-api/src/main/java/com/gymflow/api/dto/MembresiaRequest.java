package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Solicitud para asignar una membresía a un cliente")
public record MembresiaRequest(
        @Schema(description = "ID del cliente", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El cliente es obligatorio")
        Long usuarioId,

        @Schema(description = "ID del plan contratado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El plan es obligatorio")
        Long planId,

        @Schema(description = "Fecha de inicio (opcional; si se omite inicia hoy)", example = "2026-11-20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        LocalDate fechaInicio,

        @Schema(description = "Si es true registra el pago en efectivo en caja por el precio del plan", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean cobrarEnEfectivo
) {}