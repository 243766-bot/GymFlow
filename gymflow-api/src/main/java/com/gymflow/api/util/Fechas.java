package com.gymflow.api.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class Fechas {

    // Zona horaria de México: evita que "hoy" cambie antes de tiempo en un servidor con hora UTC
    public static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private Fechas() {
    }

    public static LocalDate hoy() {
        return LocalDate.now(ZONA);
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA);
    }
}