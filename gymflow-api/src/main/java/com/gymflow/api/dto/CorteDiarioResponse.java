package com.gymflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Reporte financiero consolidado del corte diario de caja")
public record CorteDiarioResponse(
        @Schema(description = "Fecha consultada del corte", example = "2026-11-20", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate fecha,

        @Schema(description = "Total de tickets de venta emitidos en el día", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
        int numeroVentas,

        @Schema(description = "Suma de ingresos por ventas de productos", example = "620.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal totalVentas,

        @Schema(description = "Ingresos por membresías cobradas en efectivo", example = "900.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal ingresosMembresiasEfectivo,

        @Schema(description = "Ingresos por membresías aprobadas vía Mercado Pago", example = "450.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal ingresosMembresiasEnLinea,

        @Schema(description = "Total de egresos/salidas registradas en caja", example = "180.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal totalSalidas,

        @Schema(description = "Balance neto del día (ventas + membresías - salidas)", example = "1790.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal balance,

        @Schema(description = "Listado resumido de ventas del día", requiredMode = Schema.RequiredMode.REQUIRED)
        List<VentaResumenResponse> ventas,

        @Schema(description = "Listado de pagos de membresía aprobados en el día", requiredMode = Schema.RequiredMode.REQUIRED)
        List<PagoResponse> pagos,

        @Schema(description = "Listado de salidas de efectivo registradas en el día", requiredMode = Schema.RequiredMode.REQUIRED)
        List<MovimientoCajaResponse> salidas,

        @Schema(description = "Efectivo disponible en caja (ventas + membres\u00edas en efectivo - salidas); no incluye pagos en l\u00ednea", example = "1340.00", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal efectivoEnCaja
) {}