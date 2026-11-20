package com.gymflow.api.controller;

import com.gymflow.api.dto.CorteDiarioResponse;
import com.gymflow.api.dto.MovimientoCajaRequest;
import com.gymflow.api.dto.MovimientoCajaResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.CajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Caja", description = "Registro de egresos/salidas de efectivo y generación de corte diario de caja. Solo ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @Operation(summary = "Registrar salida de dinero de caja", description = "Registra un egreso en efectivo (compras de insumos, pagos a proveedores, retiros). Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Salida registrada en caja",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MovimientoCajaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Concepto o monto inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/cash-movements")
    public ResponseEntity<MovimientoCajaResponse> registrarSalida(
            @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.registrarSalida(request));
    }

    @Operation(summary = "Obtener corte diario de caja", description = "Consolida ingresos por ventas, membresías en efectivo, membresías en línea, salidas y balance neto del día (si no se envía fecha, usa hoy). Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Corte diario generado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CorteDiarioResponse.class))),
            @ApiResponse(responseCode = "400", description = "Formato de fecha inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/cashreport/daily")
    public CorteDiarioResponse corteDiario(
            @Parameter(description = "Fecha del corte en formato YYYY-MM-DD (opcional, por defecto hoy)", example = "2026-11-20")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return cajaService.corteDiario(date);
    }
}