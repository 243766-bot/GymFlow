package com.gymflow.api.controller;

import com.gymflow.api.dto.FaceInfoResponse;
import com.gymflow.api.dto.FaceRegisterRequest;
import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.service.FaceAuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/{id}/face")
@PreAuthorize("hasRole('ADMIN')")
public class FaceController {

    private final FaceAuthService faceAuthService;

    public FaceController(FaceAuthService faceAuthService) {
        this.faceAuthService = faceAuthService;
    }

    // Consulta si el cliente tiene rostro registrado y, si hay, su miniatura
    @GetMapping
    public FaceInfoResponse consultar(@PathVariable Long id) {
        return faceAuthService.consultar(id);
    }

    @PostMapping
    public ResponseEntity<MensajeResponse> registrar(@PathVariable Long id,
                                                     @Valid @RequestBody FaceRegisterRequest request) {
        faceAuthService.registrar(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Rostro registrado correctamente"));
    }

    @DeleteMapping
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id) {
        faceAuthService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Rostro eliminado correctamente"));
    }
}