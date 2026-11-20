package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Solicitud de registro de una venta en mostrador")
public record VentaRequest(
        @Schema(description = "ID del cliente comprador (opcional para venta al público general)", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Long clienteId,

        @Schema(description = "Lista de productos incluidos en la venta (1 a 50 artículos distintos)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "La venta debe tener al menos un producto")
        @Size(max = 50, message = "Una venta admite hasta 50 productos distintos")
        List<@Valid VentaItemRequest> items
) {}