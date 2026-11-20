package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para inicio de sesión con correo y contraseña")
public record LoginRequest(
        @Schema(description = "Correo electrónico registrado", example = "admin@gymflow.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,

        @Schema(description = "Contraseña de la cuenta", example = "Admin12345", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {}