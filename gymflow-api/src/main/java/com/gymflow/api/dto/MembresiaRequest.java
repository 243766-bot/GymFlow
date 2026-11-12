package com.gymflow.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MembresiaRequest(
        @NotNull(message = "El cliente es obligatorio")
        Long usuarioId,

        @NotNull(message = "El plan es obligatorio")
        Long planId,

        // Opcional: si no se manda, empieza hoy
        LocalDate fechaInicio,

        // Opcional: true registra el cobro en efectivo por el precio del plan.
        // Si no se manda, no se registra ningún cobro (por ejemplo, una cortesía).
        Boolean cobrarEnEfectivo
) {
}