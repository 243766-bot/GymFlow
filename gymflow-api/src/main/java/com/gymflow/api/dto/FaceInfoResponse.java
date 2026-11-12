package com.gymflow.api.dto;

import java.time.LocalDateTime;

public record FaceInfoResponse(
        boolean registrado,
        LocalDateTime fechaRegistro,
        String foto   // null si no hay rostro o si se registró sin foto
) {
}