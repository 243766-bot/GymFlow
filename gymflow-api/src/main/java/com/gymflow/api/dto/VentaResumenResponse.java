package com.gymflow.api.dto;

import com.gymflow.api.entity.DetalleVenta;
import com.gymflow.api.entity.Venta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Resumen ejecutivo de una venta dentro del corte diario de caja")
public record VentaResumenResponse(
        @Schema(description = "ID de la venta", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Fecha y hora de la venta", example = "2026-11-20T11:20:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fecha,

        @Schema(description = "Total cobrado en MXN", example = "30.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal total,

        @Schema(description = "Administrador que atendió", example = "Administrador GymFlow", requiredMode = Schema.RequiredMode.REQUIRED)
        String atendidoPor,

        @Schema(description = "Nombre del cliente", example = "Carlos Mendoza", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String clienteNombre,

        @Schema(description = "Cantidad total de artículos vendidos", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        int articulos
) {
    public static VentaResumenResponse from(Venta v) {
        return new VentaResumenResponse(
                v.getId(), v.getFecha(), v.getTotal(), v.getAtendidoPor().getNombre(),
                v.getCliente() == null ? null : v.getCliente().getNombre(),
                v.getDetalles().stream().mapToInt(DetalleVenta::getCantidad).sum()
        );
    }
}