package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de verificación de estado de pago en línea y membresía activada")
public record PagoEstadoResponse(
        @Schema(description = "Detalle actualizado del pago", requiredMode = Schema.RequiredMode.REQUIRED)
        PagoResponse pago,

        @Schema(description = "Membresía activada o renovada (null mientras el pago no esté APROBADO)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        MembresiaResponse membresia
) {}