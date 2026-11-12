package com.gymflow.api.controller;

import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.dto.PlanRequest;
import com.gymflow.api.dto.PlanResponse;
import com.gymflow.api.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // Público: cualquiera puede ver los planes y sus precios
    @GetMapping
    public List<PlanResponse> listar() {
        return planService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanResponse> crear(@Valid @RequestBody PlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlanResponse actualizar(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return planService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id) {
        planService.eliminar(id);
        return ResponseEntity.ok(new MensajeResponse("Plan eliminado correctamente"));
    }
}
