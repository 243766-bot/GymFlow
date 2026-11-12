package com.gymflow.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 60, message = "La categoría no puede pasar de 60 caracteres")
        String categoria,

        // Opcional: por ejemplo "600 ml" o "1 kg"
        @Size(max = 40, message = "El tamaño no puede pasar de 40 caracteres")
        String tamano,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal precio,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        @Max(value = 100000, message = "El stock máximo es 100000")
        Integer stock,

        // Opcional: si no se manda, no cambia (al crear queda activo)
        Boolean activo
) {
}