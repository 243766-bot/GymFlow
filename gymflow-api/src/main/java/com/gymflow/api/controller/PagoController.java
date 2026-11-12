package com.gymflow.api.controller;

import com.gymflow.api.dto.PagoResponse;
import com.gymflow.api.service.PagoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    // Cliente: su historial de pagos
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<PagoResponse> misPagos(Authentication authentication) {
        return pagoService.misPagos(authentication.getName());
    }
}