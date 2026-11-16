package com.gymflow.api.dto;

public record CheckoutResponse(
        Long pagoId,
        String preferenciaId,
        String urlPago      // a esta dirección se manda al cliente para que pague
) {
}