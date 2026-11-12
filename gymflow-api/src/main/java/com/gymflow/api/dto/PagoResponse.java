package com.gymflow.api.dto;

import com.gymflow.api.entity.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        Long membresiaId,
        String concepto,
        BigDecimal monto,
        String metodo,     // EFECTIVO o MERCADO_PAGO
        String estado,     // APROBADO, PENDIENTE o RECHAZADO
        LocalDateTime fecha
) {
    public static PagoResponse from(Pago p) {
        return new PagoResponse(
                p.getId(),
                p.getUsuario().getId(),
                p.getUsuario().getNombre(),
                p.getMembresia() == null ? null : p.getMembresia().getId(),
                p.getConcepto(),
                p.getMonto(),
                p.getMetodo().name(),
                p.getEstado().name(),
                p.getFecha()
        );
    }
}