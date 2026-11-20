package com.gymflow.api.dto;

import com.gymflow.api.entity.Membresia;
import com.gymflow.api.entity.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Vista administrativa completa de un cliente incluyendo estado biométrico y membresía")
public record ClienteResponse(
        @Schema(description = "ID único del cliente", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        Long id,

        @Schema(description = "Nombre completo", example = "Carlos Mendoza", requiredMode = Schema.RequiredMode.REQUIRED)
        String nombre,

        @Schema(description = "Correo electrónico", example = "carlos@gymflow.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Teléfono a 10 dígitos", example = "9671234567", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String telefono,

        @Schema(description = "Rol asignado", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String rol,

        @Schema(description = "Estado de cuenta (false = baneado)", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean activo,

        @Schema(description = "Fecha y hora de registro", example = "2026-11-01T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime fechaRegistro,

        @Schema(description = "Indica si cuenta con rostro enrolado", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean rostroRegistrado,

        @Schema(description = "Membresía actual o más reciente (null si nunca ha tenido)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        MembresiaResponse membresia
) {
    public static ClienteResponse of(Usuario u, Membresia membresia, boolean rostroRegistrado) {
        return new ClienteResponse(
                u.getId(), u.getNombre(), u.getEmail(), u.getTelefono(),
                u.getRol().getNombre(), u.isActivo(), u.getFechaRegistro(),
                rostroRegistrado, membresia == null ? null : MembresiaResponse.from(membresia)
        );
    }
}