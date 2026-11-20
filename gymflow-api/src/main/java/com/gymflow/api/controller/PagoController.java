package com.gymflow.api.controller;

import com.gymflow.api.dto.CheckoutRequest;
import com.gymflow.api.dto.CheckoutResponse;
import com.gymflow.api.dto.PagoEstadoResponse;
import com.gymflow.api.dto.PagoResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.PagoEnLineaService;
import com.gymflow.api.service.PagoService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Pagos", description = "Historial de pagos del cliente y generación/verificación de cobros en línea con Mercado Pago.")
@SecurityRequirement(name = "bearerAuth")
public class PagoController {

    private final PagoService pagoService;
    private final PagoEnLineaService pagoEnLineaService;

    public PagoController(PagoService pagoService, PagoEnLineaService pagoEnLineaService) {
        this.pagoService = pagoService;
        this.pagoEnLineaService = pagoEnLineaService;
    }

    @Operation(summary = "Consultar historial de pagos propios", description = "Devuelve todos los pagos (en efectivo y en línea) realizados por el cliente autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de pagos obtenido",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PagoResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Requiere rol USER",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<PagoResponse> misPagos(Authentication authentication) {
        return pagoService.misPagos(authentication.getName());
    }

    @Operation(summary = "Iniciar checkout de pago en línea", description = "Crea una preferencia de pago en Mercado Pago para el plan seleccionado y devuelve la URL de pago.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Preferencia de pago creada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = CheckoutResponse.class))),
            @ApiResponse(responseCode = "400", description = "Plan inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol USER",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "502", description = "Error al comunicarse con la API de Mercado Pago",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                     Authentication authentication) {
        CheckoutResponse respuesta = pagoEnLineaService.crearCobro(authentication.getName(), request.planId());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @Operation(summary = "Consultar estado de un pago en línea", description = "Verifica en Mercado Pago si el pago fue aprobado; si se aprobó, activa o renueva la membresía automáticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado del pago consultado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagoEstadoResponse.class))),
            @ApiResponse(responseCode = "403", description = "No tienes permiso para consultar este pago",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "502", description = "Error al consultar Mercado Pago",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PagoEstadoResponse estado(@Parameter(description = "ID interno del pago", example = "10") @PathVariable Long id,
                                     Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return pagoEnLineaService.consultarEstado(id, authentication.getName(), esAdmin);
    }
}