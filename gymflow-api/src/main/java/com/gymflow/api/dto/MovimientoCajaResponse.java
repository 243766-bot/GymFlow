package com.gymflow.api.dto;

import com.gymflow.api.entity.MovimientoCaja;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Registro de movimiento de caja")
public record MovimientoCajaResponse(
        @Schema(description = "ID único del movimiento", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Tipo de movimiento", example = "SALIDA", allowableValues = {"SALIDA"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String tipo,

        @Schema(description = "Concepto del movimiento", example = "Compra de garrafones de agua", requiredMode = Schema.RequiredMode.REQUIRED)
        String concepto,

        @Schema(description = "Monto del movimiento en MXN", example = "180.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal monto,

        @Schema(description = "Fecha y hora en que se registró", example = "2026-11-20T10:15:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fecha
) {
    public static MovimientoCajaResponse from(MovimientoCaja m) {
        return new MovimientoCajaResponse(
                m.getId(), m.getTipo().name(), m.getConcepto(), m.getMonto(), m.getFecha());
    }
}