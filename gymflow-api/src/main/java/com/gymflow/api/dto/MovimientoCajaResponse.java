package com.gymflow.api.dto;

import com.gymflow.api.entity.MovimientoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoCajaResponse(
        Long id,
        String tipo,
        String concepto,
        BigDecimal monto,
        LocalDateTime fecha
) {
    public static MovimientoCajaResponse from(MovimientoCaja m) {
        return new MovimientoCajaResponse(
                m.getId(), m.getTipo().name(), m.getConcepto(), m.getMonto(), m.getFecha());
    }
}