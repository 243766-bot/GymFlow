package com.gymflow.api.controller;

import com.gymflow.api.dto.AuthResponse;
import com.gymflow.api.dto.FaceChallengeResponse;
import com.gymflow.api.dto.FaceLoginRequest;
import com.gymflow.api.dto.FaceLoginResponse;
import com.gymflow.api.dto.MembresiaResponse;
import com.gymflow.api.exception.ApiError;
import com.gymflow.api.service.FaceAuthService;
import com.gymflow.api.service.FaceChallengeService;
import com.gymflow.api.service.MembresiaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/face")
@Tag(name = "Login facial", description = "Desafío criptográfico de un solo uso e inicio de sesión biométrico facial. Públicos.")
public class FaceAuthController {

    private final FaceChallengeService challengeService;
    private final FaceAuthService faceAuthService;
    private final MembresiaService membresiaService;

    public FaceAuthController(FaceChallengeService challengeService,
                              FaceAuthService faceAuthService,
                              MembresiaService membresiaService) {
        this.challengeService = challengeService;
        this.faceAuthService = faceAuthService;
        this.membresiaService = membresiaService;
    }

    @Operation(summary = "Solicitar desafío facial (Challenge)", description = "Genera un token aleatorio de un solo uso con vigencia limitada (TTL) para prevenir ataques de repetición.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Desafío generado correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaceChallengeResponse.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/challenge")
    public ResponseEntity<FaceChallengeResponse> challenge() {
        String desafio = challengeService.crear();
        return ResponseEntity.ok(new FaceChallengeResponse(desafio, challengeService.getTtlSegundos()));
    }

    @Operation(summary = "Iniciar sesión con reconocimiento facial", description = "Valida el desafío y compara el descriptor facial de 128 dimensiones contra las plantillas registradas. Devuelve JWT, datos del usuario y estado de su membresía.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rostro reconocido e inicio de sesión exitoso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FaceLoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Descriptor inválido o desafío expirado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Rostro no reconocido o desafío inválido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Cuenta suspendida o límite de intentos excedido",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<FaceLoginResponse> login(@Valid @RequestBody FaceLoginRequest request,
                                                   HttpServletRequest http) {
        AuthResponse auth = faceAuthService.login(request, http.getRemoteAddr());
        MembresiaResponse membresia = membresiaService
                .actualDeUsuario(auth.usuario().id())
                .orElse(null);

        return ResponseEntity.ok(
                new FaceLoginResponse(auth.token(), auth.tipo(), auth.usuario(), membresia));
    }
}