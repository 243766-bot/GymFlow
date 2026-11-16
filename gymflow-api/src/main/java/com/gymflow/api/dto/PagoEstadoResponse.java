package com.gymflow.api.dto;

public record PagoEstadoResponse(
        PagoResponse pago,
        MembresiaResponse membresia   // null mientras el pago no esté aprobado
) {
}