package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un plan de membresía")
public record PlanRequest(
        @Schema(description = "Nombre comercial del plan", example = "Mensualidad Estándar", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede pasar de 80 caracteres")
        String nombre,

        @Schema(description = "Duración de vigencia en días (1 a 3650)", example = "30", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "La duración en días es obligatoria")
        @Min(value = 1, message = "La duración mínima es de 1 día")
        @Max(value = 3650, message = "La duración máxima es de 3650 días")
        Integer duracionDias,

        @Schema(description = "Precio del plan en MXN", example = "450.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal precio
) {}