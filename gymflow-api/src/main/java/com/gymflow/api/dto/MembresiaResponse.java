package com.gymflow.api.dto;

import com.gymflow.api.entity.EstadoMembresia;
import com.gymflow.api.entity.Membresia;
import com.gymflow.api.util.Fechas;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record MembresiaResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        PlanResponse plan,
        LocalDate fechaInicio,
        LocalDate fechaVencimiento,
        String estado,        // ACTIVA, VENCIDA o CANCELADA
        long diasRestantes
) {
    public static MembresiaResponse from(Membresia m) {
        LocalDate hoy = Fechas.hoy();

        // El estado se calcula al responder, así nunca queda desactualizado
        String estado;
        if (m.getEstado() == EstadoMembresia.CANCELADA) {
            estado = "CANCELADA";
        } else if (hoy.isAfter(m.getFechaVencimiento())) {
            estado = "VENCIDA";
        } else {
            estado = "ACTIVA";
        }

        long dias = estado.equals("ACTIVA")
                ? ChronoUnit.DAYS.between(hoy, m.getFechaVencimiento())
                : 0;

        return new MembresiaResponse(
                m.getId(),
                m.getUsuario().getId(),
                m.getUsuario().getNombre(),
                PlanResponse.from(m.getPlan()),
                m.getFechaInicio(),
                m.getFechaVencimiento(),
                estado,
                dias
        );
    }
}