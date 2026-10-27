package com.gymflow.api.dto;

public record AuthResponse(
        String token,
        String tipo,
        UsuarioResponse usuario
) {
}