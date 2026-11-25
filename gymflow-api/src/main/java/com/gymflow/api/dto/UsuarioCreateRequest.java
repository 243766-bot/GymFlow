package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.gymflow.api.util.Validaciones;

@Schema(description = "Datos para creación de usuarios o administradores por parte de un ADMIN")
public record UsuarioCreateRequest(
        @Schema(description = "Nombre completo del usuario", example = "Ana López", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede pasar de 100 caracteres")
        @Pattern(regexp = "^[\\p{L}][\\p{L}\\s.'’-]*$", message = "El nombre solo puede llevar letras, espacios, punto, guion y apóstrofo")
        String nombre,

        @Schema(description = "Correo electrónico único", example = "ana.lopez@gymflow.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 150, message = "El correo no puede pasar de 150 caracteres")
        @Pattern(regexp = Validaciones.EMAIL, message = Validaciones.EMAIL_MENSAJE)
        String email,

        @Schema(description = "Contraseña (8 a 72 caracteres, letras y números)", example = "AnaGym2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es obligatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[\\x20-\\x7E]{8,72}$", message = "La contraseña debe tener de 8 a 72 caracteres, con al menos una letra y un número, sin acentos ni emojis")
        String password,

        @Schema(description = "Teléfono celular de 10 dígitos (opcional)", example = "9679876543", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "^([0-9]{10})?$", message = "El teléfono debe tener exactamente 10 dígitos, solo números")
        String telefono,

        @Schema(description = "Rol asignado al usuario (por defecto ROLE_USER)", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_ADMIN"}, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "ROLE_USER|ROLE_ADMIN", message = "El rol debe ser ROLE_USER o ROLE_ADMIN")
        String rol
) {}