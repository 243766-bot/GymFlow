package com.gymflow.api.controller;

import com.gymflow.api.dto.CheckoutRequest;
import com.gymflow.api.dto.CheckoutResponse;
import com.gymflow.api.dto.PagoEstadoResponse;
import com.gymflow.api.dto.PagoResponse;
import com.gymflow.api.service.PagoEnLineaService;
import com.gymflow.api.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PagoController {

    private final PagoService pagoService;
    private final PagoEnLineaService pagoEnLineaService;

    public PagoController(PagoService pagoService, PagoEnLineaService pagoEnLineaService) {
        this.pagoService = pagoService;
        this.pagoEnLineaService = pagoEnLineaService;
    }

    // Cliente: su historial de pagos
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<PagoResponse> misPagos(Authentication authentication) {
        return pagoService.misPagos(authentication.getName());
    }

    // Cliente: pedir pagar un plan en línea. Devuelve el enlace de Mercado Pago.
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                     Authentication authentication) {
        CheckoutResponse respuesta = pagoEnLineaService.crearCobro(authentication.getName(), request.planId());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // Cliente (su pago) o administrador (cualquiera): consulta el estado en Mercado Pago
    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PagoEstadoResponse estado(@PathVariable Long id, Authentication authentication) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return pagoEnLineaService.consultarEstado(id, authentication.getName(), esAdmin);
    }
}