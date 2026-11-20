package com.gymflow.api.dto;

import com.gymflow.api.entity.Pago;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Registro de pago de membresía")
public record PagoResponse(
        @Schema(description = "ID único del pago", example = "14", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "ID del cliente que realizó el pago", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Long usuarioId,

        @Schema(description = "Nombre del cliente", example = "Carlos Mendoza", requiredMode = Schema.RequiredMode.REQUIRED)
        String usuarioNombre,

        @Schema(description = "ID de la membresía asociada (null si el pago sigue pendiente)", example = "5", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long membresiaId,

        @Schema(description = "Concepto cobrado", example = "Membresía: Mensualidad Estándar", requiredMode = Schema.RequiredMode.REQUIRED)
        String concepto,

        @Schema(description = "Monto cobrado en MXN", example = "450.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal monto,

        @Schema(description = "Método de pago utilizado", example = "MERCADO_PAGO", allowableValues = {"EFECTIVO", "MERCADO_PAGO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String metodo,

        @Schema(description = "Estado del pago", example = "APROBADO", allowableValues = {"APROBADO", "PENDIENTE", "RECHAZADO"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String estado,

        @Schema(description = "Fecha y hora del pago", example = "2026-11-20T12:40:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fecha
) {
    public static PagoResponse from(Pago p) {
        return new PagoResponse(
                p.getId(), p.getUsuario().getId(), p.getUsuario().getNombre(),
                p.getMembresia() == null ? null : p.getMembresia().getId(),
                p.getConcepto(), p.getMonto(), p.getMetodo().name(), p.getEstado().name(), p.getFecha()
        );
    }
}