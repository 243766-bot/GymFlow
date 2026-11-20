package com.gymflow.api.controller;

import com.gymflow.api.dto.VentaRequest;
import com.gymflow.api.dto.VentaResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sales")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Ventas", description = "Punto de venta (POS): registro y consulta de ventas con descuento automático de inventario. Solo ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Operation(summary = "Registrar una nueva venta", description = "Procesa la venta de uno o varios productos, descuenta el stock automáticamente y calcula el total. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta registrada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente, producto inactivo o lista vacía",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Producto o cliente no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request,
                                                   Authentication authentication) {
        VentaResponse venta = ventaService.registrar(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @Operation(summary = "Listar ventas realizadas", description = "Obtiene el historial de ventas, con filtro opcional por fecha exacta (YYYY-MM-DD). Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de ventas obtenido",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VentaResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Formato de fecha inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public List<VentaResponse> listar(
            @Parameter(description = "Filtrar ventas por fecha (ISO-8601)", example = "2026-11-20")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ventaService.listar(fecha);
    }

    @Operation(summary = "Obtener ticket/detalle de una venta", description = "Consulta el desglose completo de artículos vendidos por ID de venta. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public VentaResponse obtener(@Parameter(description = "ID de la venta", example = "1") @PathVariable Long id) {
        return ventaService.obtener(id);
    }
}