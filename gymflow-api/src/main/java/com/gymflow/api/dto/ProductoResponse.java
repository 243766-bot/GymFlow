package com.gymflow.api.dto;

import com.gymflow.api.entity.Producto;

import java.math.BigDecimal;

public record ProductoResponse(
        Long id,
        String nombre,
        String categoria,
        String tamano,
        BigDecimal precio,
        int stock,
        boolean activo
) {
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getCategoria(), p.getTamano(),
                p.getPrecio(), p.getStock(), p.isActivo());
    }
}