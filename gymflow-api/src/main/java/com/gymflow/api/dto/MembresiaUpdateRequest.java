package com.gymflow.api.dto;

import com.gymflow.api.entity.EstadoMembresia;

import java.time.LocalDate;

public record MembresiaUpdateRequest(
        Long planId,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        EstadoMembresia estado
) {
}