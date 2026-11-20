package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Datos para enrolar la plantilla facial de un cliente")
public record FaceRegisterRequest(
        @Schema(description = "Vector descriptor facial de exactamente 128 valores numéricos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El descriptor es obligatorio")
        @Size(min = 128, max = 128, message = "El descriptor debe tener exactamente 128 valores")
        List<@NotNull Double> descriptor,

        @Schema(description = "Confirmación explícita de consentimiento biométrico del cliente", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Debes indicar si el cliente dio su consentimiento")
        Boolean consentimiento,

        @Schema(description = "Miniatura JPEG en formato Data URL Base64 (opcional)", example = "data:image/jpeg;base64,/9j/4AAQSkZJRg...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 300000, message = "La foto es demasiado grande")
        String foto
) {}