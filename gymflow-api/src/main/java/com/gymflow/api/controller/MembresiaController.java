package com.gymflow.api.controller;

import com.gymflow.api.dto.MembresiaRequest;
import com.gymflow.api.dto.MembresiaResponse;
import com.gymflow.api.dto.MembresiaUpdateRequest;
import com.gymflow.api.dto.RenovarRequest;
import com.gymflow.api.service.MembresiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/memberships")
public class MembresiaController {

    private final MembresiaService membresiaService;

    public MembresiaController(MembresiaService membresiaService) {
        this.membresiaService = membresiaService;
    }

    // Administrador: todas las membresías (opcional: ?usuarioId=3 para filtrar por cliente)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MembresiaResponse> listar(@RequestParam(required = false) Long usuarioId) {
        return membresiaService.listar(usuarioId);
    }

    // Cliente: su propia membresía
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public MembresiaResponse miMembresia(Authentication authentication) {
        return membresiaService.miMembresia(authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MembresiaResponse> crear(@Valid @RequestBody MembresiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membresiaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MembresiaResponse actualizar(@PathVariable Long id,
                                        @RequestBody MembresiaUpdateRequest request) {
        return membresiaService.actualizar(id, request);
    }

    // El cuerpo es opcional: sin cuerpo renueva con el mismo plan
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public MembresiaResponse renovar(@PathVariable Long id,
                                     @RequestBody(required = false) RenovarRequest request) {
        return membresiaService.renovar(id, request);
    }
}