package com.gymflow.api.controller;

import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.dto.PlanRequest;
import com.gymflow.api.dto.PlanResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.PlanService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "Planes", description = "Catálogo de planes de membresía. La consulta es pública; creación, edición y borrado requieren ADMIN.")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @Operation(summary = "Listar planes disponibles", description = "Endpoint público que devuelve todos los planes de membresía activos con su duración y precio.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de planes obtenida",
                    content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PlanResponse.class)))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public List<PlanResponse> listar() {
        return planService.listar();
    }

    @Operation(summary = "Crear nuevo plan de membresía", description = "Registra un plan con nombre único, duración en días y precio. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plan creado exitosamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o nombre de plan duplicado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> crear(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.crear(request));
    }

    @Operation(summary = "Actualizar un plan existente", description = "Modifica el nombre, duración o precio de un plan por su ID. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan actualizado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlanResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlanResponse actualizar(@Parameter(description = "ID del plan", example = "1") @PathVariable Long id,
                                   @Valid @RequestBody PlanRequest request) {
        return planService.actualizar(id, request);
    }

    @Operation(summary = "Eliminar un plan", description = "Elimina un plan si no tiene membresías o pagos asociados. Solo ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan eliminado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensajeResponse.class))),
            @ApiResponse(responseCode = "400", description = "El plan tiene registros asociados",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Plan no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> eliminar(@Parameter(description = "ID del plan", example = "1") @PathVariable Long id) {
        planService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Plan eliminado correctamente"));
    }
}