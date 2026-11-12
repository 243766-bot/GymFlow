package com.gymflow.api.dto;

// Todo es opcional: sin cuerpo, renueva con el mismo plan y sin registrar cobro
public record RenovarRequest(
        Long planId,
        Boolean cobrarEnEfectivo
) {
}