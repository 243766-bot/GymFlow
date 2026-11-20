package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un producto en inventario")
public record ProductoRequest(
        @Schema(description = "Nombre del producto", example = "Proteína Whey Gold Standard", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @Schema(description = "Categoría del producto", example = "Suplementos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 60, message = "La categoría no puede pasar de 60 caracteres")
        String categoria,

        @Schema(description = "Presentación o tamaño (opcional)", example = "907 g", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 40, message = "El tamaño no puede pasar de 40 caracteres")
        String tamano,

        @Schema(description = "Precio unitario de venta en MXN", example = "890.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal precio,

        @Schema(description = "Cantidad disponible en inventario", example = "15", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(value = 100000, message = "El stock máximo es 100000")
        Integer stock,

        @Schema(description = "Indica si el producto está disponible para la venta", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean activo
) {}