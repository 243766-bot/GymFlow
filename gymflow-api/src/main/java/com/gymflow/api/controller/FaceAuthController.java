package com.gymflow.api.controller;

import com.gymflow.api.dto.AuthResponse;
import com.gymflow.api.dto.FaceChallengeResponse;
import com.gymflow.api.dto.FaceLoginRequest;
import com.gymflow.api.service.FaceAuthService;
import com.gymflow.api.service.FaceChallengeService;
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

    public FaceAuthController(FaceChallengeService challengeService, FaceAuthService faceAuthService) {
        this.challengeService = challengeService;
        this.faceAuthService = faceAuthService;
    }

    @PostMapping("/challenge")
    public ResponseEntity<FaceChallengeResponse> challenge() {
        String desafio = challengeService.crear();
        return ResponseEntity.ok(new FaceChallengeResponse(desafio, challengeService.getTtlSegundos()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody FaceLoginRequest request,
                                              HttpServletRequest http) {
        // El dispositivo se identifica por su IP
        return ResponseEntity.ok(faceAuthService.login(request, http.getRemoteAddr()));
    }
}