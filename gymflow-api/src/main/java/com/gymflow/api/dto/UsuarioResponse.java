package com.gymflow.api.dto;

import com.gymflow.api.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Información pública del perfil de usuario (excluye contraseña)")
public record UsuarioResponse(
        @Schema(description = "ID único del usuario", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Nombre completo", example = "Carlos Mendoza", requiredMode = Schema.RequiredMode.REQUIRED)
        String nombre,

        @Schema(description = "Correo electrónico", example = "carlos@gymflow.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Teléfono a 10 dígitos", example = "9671234567", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String telefono,

        @Schema(description = "Rol del usuario en el sistema", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String rol,

        @Schema(description = "Indica si la cuenta está activa (false = baneado)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean activo,

        @Schema(description = "Fecha y hora de creación de la cuenta", example = "2026-11-01T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fechaRegistro
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(),
                u.getRol().getNombre(), u.isActivo(), u.getFechaRegistro()
        );
    }
}