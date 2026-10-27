package com.gymflow.api.dto;

public record FaceChallengeResponse(
        String challenge,
        long expiraEnSegundos
) {
}