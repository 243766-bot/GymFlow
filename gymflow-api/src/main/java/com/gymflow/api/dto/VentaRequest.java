package com.gymflow.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VentaRequest(
        // Opcional: el cliente al que se le vende
        Long clienteId,

        @NotEmpty(message = "La venta debe tener al menos un producto")
        @Size(max = 50, message = "Una venta admite hasta 50 productos distintos")
        List<@Valid VentaItemRequest> items
) {
}