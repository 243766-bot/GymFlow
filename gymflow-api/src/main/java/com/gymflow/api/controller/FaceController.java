package com.gymflow.api.controller;

import com.gymflow.api.dto.FaceInfoResponse;
import com.gymflow.api.dto.FaceRegisterRequest;
import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.FaceAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@RestController
@RequestMapping("/api/v1/users/{id}/face")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Rostro de clientes", description = "Registro, consulta y eliminación de plantillas biométricas faciales de los clientes. Solo ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class FaceController {

    private final FaceAuthService faceAuthService;

    public FaceController(FaceAuthService faceAuthService) {
        this.faceAuthService = faceAuthService;
    }

    @Operation(summary = "Consultar estado biométrico del cliente", description = "Indica si el cliente tiene un descriptor facial registrado, su fecha de alta y su miniatura en Base64.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Información facial consultada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaceInfoResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public FaceInfoResponse consultar(@Parameter(description = "ID del usuario", example = "2") @PathVariable Long id) {
        return faceAuthService.consultar(id);
    }

    @Operation(summary = "Registrar o actualizar rostro de un cliente", description = "Guarda el descriptor biométrico de 128 valores flotantes y el consentimiento expreso del cliente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rostro registrado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensajeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Descriptor inválido o falta consentimiento",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<MensajeResponse> registrar(@Parameter(description = "ID del usuario", example = "2") @PathVariable Long id,
                                                     @Valid @RequestBody FaceRegisterRequest request) {
        faceAuthService.registrar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Rostro registrado correctamente"));
    }

    @Operation(summary = "Eliminar plantilla facial del cliente", description = "Borra los datos biométricos almacenados del usuario indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rostro eliminado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensajeResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuario o plantilla facial no encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping
    public ResponseEntity<MensajeResponse> eliminar(@Parameter(description = "ID del usuario", example = "2") @PathVariable Long id) {
        faceAuthService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Rostro eliminado correctamente"));
    }
}