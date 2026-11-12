package com.gymflow.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CorteDiarioResponse(
        LocalDate fecha,
        int numeroVentas,
        BigDecimal totalVentas,
        BigDecimal ingresosMembresiasEfectivo,
        BigDecimal ingresosMembresiasEnLinea,
        BigDecimal totalSalidas,
        BigDecimal balance,     // ventas + membresías - salidas
        List<VentaResumenResponse> ventas,
        List<PagoResponse> pagos,
        List<MovimientoCajaResponse> salidas
) {
}