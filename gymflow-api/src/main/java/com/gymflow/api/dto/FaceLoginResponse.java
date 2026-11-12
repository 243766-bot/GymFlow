package com.gymflow.api.dto;

public record FaceLoginResponse(
        String token,
        String tipo,
        UsuarioResponse usuario,
        MembresiaResponse membresia   // null si el cliente aún no tiene membresía
) {
}