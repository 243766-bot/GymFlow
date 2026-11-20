package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Renglón de producto y cantidad dentro de una venta")
public record VentaItemRequest(
        @Schema(description = "ID del producto a vender", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El producto es obligatorio")
        Long productoId,

        @Schema(description = "Unidades vendidas (1 a 1000)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad mínima es 1")
        @Max(value = 1000, message = "La cantidad máxima por producto es 1000")
        Integer cantidad
) {}