package com.gymflow.api.controller;

import com.gymflow.api.dto.CorteDiarioResponse;
import com.gymflow.api.dto.MovimientoCajaRequest;
import com.gymflow.api.dto.MovimientoCajaResponse;
import com.gymflow.api.service.CajaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('ADMIN')")
public class CajaController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    // Registra una salida de dinero (compras, pagos, retiros...)
    @PostMapping("/cash-movements")
    public ResponseEntity<MovimientoCajaResponse> registrarSalida(
            @Valid @RequestBody MovimientoCajaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.registrarSalida(request));
    }

    // Corte del día: /cashreport/daily?date=2026-11-02 (sin fecha usa hoy)
    @GetMapping("/cashreport/daily")
    public CorteDiarioResponse corteDiario(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return cajaService.corteDiario(date);
    }
}