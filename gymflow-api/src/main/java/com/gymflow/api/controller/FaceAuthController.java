package com.gymflow.api.controller;

import com.gymflow.api.dto.AuthResponse;
import com.gymflow.api.dto.FaceChallengeResponse;
import com.gymflow.api.dto.FaceLoginRequest;
import com.gymflow.api.dto.FaceLoginResponse;
import com.gymflow.api.dto.MembresiaResponse;
import com.gymflow.api.service.FaceAuthService;
import com.gymflow.api.service.FaceChallengeService;
import com.gymflow.api.service.MembresiaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/face")
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

    @PostMapping("/challenge")
    public ResponseEntity<FaceChallengeResponse> challenge() {
        String desafio = challengeService.crear();
        return ResponseEntity.ok(new FaceChallengeResponse(desafio, challengeService.getTtlSegundos()));
    }

    @PostMapping("/login")
    public ResponseEntity<FaceLoginResponse> login(@Valid @RequestBody FaceLoginRequest request,
                                                   HttpServletRequest http) {
        // El dispositivo se identifica por su IP
        AuthResponse auth = faceAuthService.login(request, http.getRemoteAddr());

        // Para la pantalla de la caja: estado, vencimiento y plan de la membresía
        MembresiaResponse membresia = membresiaService
                .actualDeUsuario(auth.usuario().id())
                .orElse(null);

        return ResponseEntity.ok(
                new FaceLoginResponse(auth.token(), auth.tipo(), auth.usuario(), membresia));
    }
}