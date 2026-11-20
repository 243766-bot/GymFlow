package com.gymflow.api.dto;

import com.gymflow.api.entity.Venta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Comprobante detallado de una venta")
public record VentaResponse(
        @Schema(description = "ID único de la venta", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Fecha y hora de la venta", example = "2026-11-20T11:20:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fecha,

        @Schema(description = "Importe total de la venta en MXN", example = "30.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal total,

        @Schema(description = "Nombre del administrador que registró la venta", example = "Administrador GymFlow", requiredMode = Schema.RequiredMode.REQUIRED)
        String atendidoPor,

        @Schema(description = "ID del cliente (null si fue venta general)", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long clienteId,

        @Schema(description = "Nombre del cliente (null si fue venta general)", example = "Carlos Mendoza", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String clienteNombre,

        @Schema(description = "Lista de productos vendidos", requiredMode = Schema.RequiredMode.REQUIRED)
        List<DetalleVentaResponse> detalles
) {
    public static VentaResponse from(Venta v) {
        return new VentaResponse(
                v.getId(), v.getFecha(), v.getTotal(), v.getAtendidoPor().getNombre(),
                v.getCliente() == null ? null : v.getCliente().getId(),
                v.getCliente() == null ? null : v.getCliente().getNombre(),
                v.getDetalles().stream().map(DetalleVentaResponse::from).toList()
        );
    }
}