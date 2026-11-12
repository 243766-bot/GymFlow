package com.gymflow.api.dto;

import com.gymflow.api.entity.Plan;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String nombre,
        int duracionDias,
        BigDecimal precio
) {
    public static PlanResponse from(Plan p) {
        return new PlanResponse(p.getId(), p.getNombre(), p.getDuracionDias(), p.getPrecio());
    }
}