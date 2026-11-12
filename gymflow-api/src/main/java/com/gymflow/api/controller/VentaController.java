package com.gymflow.api.controller;

import com.gymflow.api.dto.VentaRequest;
import com.gymflow.api.dto.VentaResponse;
import com.gymflow.api.service.VentaService;
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
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request,
                                                   Authentication authentication) {
        VentaResponse venta = ventaService.registrar(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    // Opcional: /sales?fecha=2026-11-02 para ver solo las ventas de ese día
    @GetMapping
    public List<VentaResponse> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ventaService.listar(fecha);
    }

    @GetMapping("/{id}")
    public VentaResponse obtener(@PathVariable Long id) {
        return ventaService.obtener(id);
    }
}