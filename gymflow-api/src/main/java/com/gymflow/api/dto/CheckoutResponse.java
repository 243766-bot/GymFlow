package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de redirección a la pasarela de pago Mercado Pago")
public record CheckoutResponse(
        @Schema(description = "ID interno del registro de pago", example = "14", requiredMode = Schema.RequiredMode.REQUIRED)
        Long pagoId,

        @Schema(description = "Identificador de preferencia en Mercado Pago", example = "3847055086-a1b2c3d4-e5f6", requiredMode = Schema.RequiredMode.REQUIRED)
        String preferenciaId,

        @Schema(description = "URL de Checkout Pro a la cual redirigir al cliente", example = "https://www.mercadopago.com.mx/checkout/v1/redirect?pref_id=...", requiredMode = Schema.RequiredMode.REQUIRED)
        String urlPago
) {}