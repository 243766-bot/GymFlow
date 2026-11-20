package com.gymflow.api.controller;

import com.gymflow.api.dto.MembresiaRequest;
import com.gymflow.api.dto.MembresiaResponse;
import com.gymflow.api.dto.MembresiaUpdateRequest;
import com.gymflow.api.dto.RenovarRequest;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.MembresiaService;
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
@RequestMapping("/api/v1/memberships")
@Tag(name = "Membresías", description = "Asignación, edición y renovación de membresías (ADMIN) y consulta de membresía propia (USER).")
@SecurityRequirement(name = "bearerAuth")
public class MembresiaController {

    private final MembresiaService membresiaService;

    public MembresiaController(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    @Operation(summary = "Listar membresías", description = "Devuelve todas las membresías registradas, con filtro opcional por ID de cliente. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de membresías obtenido",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MembresiaResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MembresiaResponse> listar(
            @Parameter(description = "Filtrar membresías por ID del cliente", example = "3") @RequestParam(required = false) Long usuarioId) {
        return membresiaService.listar(usuarioId);
    }

    @Operation(summary = "Consultar mi membresía actual", description = "Devuelve la membresía activa o más reciente del cliente autenticado (ROLE_USER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membresía encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol USER",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "El cliente aún no tiene membresías registradas",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public MembresiaResponse miMembresia(Authentication authentication) {
        return membresiaService.miMembresia(authentication.getName());
    }

    @Operation(summary = "Asignar nueva membresía a un cliente", description = "Crea una membresía para un usuario y opcionalmente registra el pago en efectivo en caja. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membresía creada exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o el cliente ya posee una membresía activa",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario o plan no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembresiaResponse> crear(@Valid @RequestBody MembresiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membresiaService.crear(request));
    }

    @Operation(summary = "Actualizar datos de una membresía", description = "Modifica el plan, fechas o estado (ACTIVA/CANCELADA) de una membresía. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membresía actualizada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Fechas inconsistentes o datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Membresía o plan no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MembresiaResponse actualizar(@Parameter(description = "ID de la membresía", example = "1") @PathVariable Long id,
                                        @RequestBody MembresiaUpdateRequest request) {
        return membresiaService.actualizar(id, request);
    }

    @Operation(summary = "Renovar una membresía", description = "Extiende la vigencia de la membresía con el mismo plan u otro nuevo, y permite registrar el cobro en efectivo. El cuerpo JSON es opcional. Solo ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membresía renovada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MembresiaResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Membresía o plan no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public MembresiaResponse renovar(@Parameter(description = "ID de la membresía", example = "1") @PathVariable Long id,
                                     @RequestBody(required = false) RenovarRequest request) {
        return membresiaService.renovar(id, request);
    }
}