package com.gymflow.api.entity;

// Estado guardado. "VENCIDA" no se guarda: se calcula comparando con la fecha de hoy.
public enum EstadoMembresia {
    ACTIVA,
    CANCELADA
}