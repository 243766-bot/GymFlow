package com.gymflow.api.dto;

import com.gymflow.api.entity.DetalleVenta;
import com.gymflow.api.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VentaResumenResponse(
        Long id,
        LocalDateTime fecha,
        BigDecimal total,
        String atendidoPor,
        String clienteNombre,
        int articulos
) {
    public static VentaResumenResponse from(Venta v) {
        return new VentaResumenResponse(
                v.getId(),
                v.getFecha(),
                v.getTotal(),
                v.getAtendidoPor().getNombre(),
                v.getCliente() == null ? null : v.getCliente().getNombre(),
                v.getDetalles().stream().mapToInt(DetalleVenta::getCantidad).sum()
        );
    }
}