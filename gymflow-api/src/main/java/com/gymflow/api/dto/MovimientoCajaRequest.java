package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Solicitud para registrar una salida o gasto de efectivo en caja")
public record MovimientoCajaRequest(
        @Schema(description = "Motivo o descripción del egreso", example = "Compra de garrafones de agua y artículos de limpieza", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El concepto es obligatorio")
        @Size(max = 150, message = "El concepto no puede pasar de 150 caracteres")
        String concepto,

        @Schema(description = "Monto retirado en MXN", example = "180.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El monto admite hasta 2 decimales")
        BigDecimal monto
) {}