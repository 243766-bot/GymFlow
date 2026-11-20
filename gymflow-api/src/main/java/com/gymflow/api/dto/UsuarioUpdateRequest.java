package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para actualizar la información de un usuario")
public record UsuarioUpdateRequest(
        @Schema(description = "Nombre actualizado del usuario", example = "Ana María López", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        @Pattern(regexp = "^[\\p{L}][\\p{L}\\s.'’-]*$", message = "El nombre solo puede llevar letras, espacios, punto, guion y apóstrofo")
        String nombre,

        @Schema(description = "Correo electrónico actualizado", example = "ana.lopez@gymflow.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede pasar de 150 caracteres")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$", message = "El correo debe tener un formato como nombre@dominio.com")
        String email,

        @Schema(description = "Teléfono celular de 10 dígitos", example = "9679876543", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "^([0-9]{10})?$", message = "El teléfono debe tener exactamente 10 dígitos, solo números")
        String telefono,

        @Schema(description = "Nueva contraseña (opcional; si se omite o va vacía no se modifica)", example = "NuevaPass2026", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)[\\x20-\\x7E]{8,72}$", message = "La contraseña debe tener de 8 a 72 caracteres, con al menos una letra y un número, sin acentos ni emojis")
        String password
) {}