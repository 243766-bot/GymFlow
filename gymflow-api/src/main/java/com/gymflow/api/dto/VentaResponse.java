package com.gymflow.api.dto;

import com.gymflow.api.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
        Long id,
        LocalDateTime fecha,
        BigDecimal total,
        String atendidoPor,
        Long clienteId,
        String clienteNombre,
        List<DetalleVentaResponse> detalles
) {
    public static VentaResponse from(Venta v) {
        return new VentaResponse(
                v.getId(),
                v.getFecha(),
                v.getTotal(),
                v.getAtendidoPor().getNombre(),
                v.getCliente() == null ? null : v.getCliente().getId(),
                v.getCliente() == null ? null : v.getCliente().getNombre(),
                v.getDetalles().stream().map(DetalleVentaResponse::from).toList()
        );
    }
}