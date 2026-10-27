package com.gymflow.api.dto;

import com.gymflow.api.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        String telefono,
        String rol,
        boolean activo,
        LocalDateTime fechaRegistro
) {
    // Convierte la entidad en DTO. Fíjate que NO incluye la contraseña.
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol().getNombre(),
                u.isActivo(),
                u.getFechaRegistro()
        );
    }
}