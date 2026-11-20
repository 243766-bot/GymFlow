package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parámetros opcionales para renovar una membresía")
public record RenovarRequest(
        @Schema(description = "ID del plan para la renovación (si se omite conserva el actual)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long planId,

        @Schema(description = "true para registrar el cobro en efectivo en caja", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Boolean cobrarEnEfectivo
) {}