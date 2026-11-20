package com.gymflow.api.dto;

import com.gymflow.api.entity.Producto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Información de un producto del inventario")
public record ProductoResponse(
        @Schema(description = "ID único del producto", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Nombre del producto", example = "Proteína Whey Gold Standard", requiredMode = Schema.RequiredMode.REQUIRED)
        String nombre,

        @Schema(description = "Categoría", example = "Suplementos", requiredMode = Schema.RequiredMode.REQUIRED)
        String categoria,

        @Schema(description = "Tamaño o presentación", example = "907 g", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String tamano,

        @Schema(description = "Precio unitario en MXN", example = "890.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal precio,

        @Schema(description = "Existencias actuales", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
        int stock,

        @Schema(description = "Estado del producto en catálogo", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean activo
) {
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(
                p.getId(), p.getNombre(), p.getCategoria(), p.getTamano(),
                p.getPrecio(), p.getStock(), p.isActivo());
    }
}