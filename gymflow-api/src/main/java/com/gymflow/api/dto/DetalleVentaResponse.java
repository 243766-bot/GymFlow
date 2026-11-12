package com.gymflow.api.dto;

import com.gymflow.api.entity.DetalleVenta;

import java.math.BigDecimal;

public record DetalleVentaResponse(
        Long productoId,
        String producto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
    public static DetalleVentaResponse from(DetalleVenta d) {
        return new DetalleVentaResponse(
                d.getProducto().getId(),
                d.getProducto().getNombre(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
        );
    }
}