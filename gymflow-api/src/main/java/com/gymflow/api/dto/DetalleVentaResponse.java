package com.gymflow.api.dto;

import com.gymflow.api.entity.DetalleVenta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Desglose por artículo de una venta realizada")
public record DetalleVentaResponse(
        @Schema(description = "ID del producto vendido", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long productoId,

        @Schema(description = "Nombre del producto", example = "Agua Natural 600 ml", requiredMode = Schema.RequiredMode.REQUIRED)
        String producto,

        @Schema(description = "Cantidad vendida", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        int cantidad,

        @Schema(description = "Precio unitario aplicado al momento de la venta", example = "15.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal precioUnitario,

        @Schema(description = "Subtotal del renglón (cantidad * precioUnitario)", example = "30.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal subtotal
) {
    public static DetalleVentaResponse from(DetalleVenta d) {
        return new DetalleVentaResponse(
                d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                d.getPrecioUnitario(), d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()))
        );
    }
}