package com.gymflow.api.dto;

import com.gymflow.api.entity.Membresia;
import com.gymflow.api.entity.Usuario;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String rol,
        boolean activo,               // false = baneado
        LocalDateTime fechaRegistro,
        boolean rostroRegistrado,
        MembresiaResponse membresia   // null si no tiene ninguna
) {
    public static ClienteResponse of(Usuario u, Membresia membresia, boolean rostroRegistrado) {
        return new ClienteResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol().getNombre(),
                u.isActivo(),
                u.getFechaRegistro(),
                rostroRegistrado,
                membresia == null ? null : MembresiaResponse.from(membresia)
        );
    }
}