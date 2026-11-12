package com.gymflow.api.dto;

import jakarta.validation.constraints.NotNull;

public record BanRequest(
        @NotNull(message = "Indica si el usuario queda baneado (true) o reactivado (false)")
        Boolean baneado
) {
}