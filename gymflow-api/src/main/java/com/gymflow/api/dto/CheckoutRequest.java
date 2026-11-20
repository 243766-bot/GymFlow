package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Solicitud de pago en línea mediante Mercado Pago")
public record CheckoutRequest(
        @Schema(description = "ID del plan a pagar en línea", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El plan es obligatorio")
        Long planId
) {}