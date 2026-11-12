package com.gymflow.api.controller;

import com.gymflow.api.dto.BanRequest;
import com.gymflow.api.dto.ClienteResponse;
import com.gymflow.api.dto.MensajeResponse;
import com.gymflow.api.dto.UsuarioCreateRequest;
import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.dto.UsuarioUpdateRequest;
import com.gymflow.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Cualquier usuario con sesión: su propio perfil
    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return usuarioService.perfil(authentication.getName());
    }

    // ---------- Solo administrador ----------

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ClienteResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse banear(@PathVariable Long id,
                                  @Valid @RequestBody BanRequest request,
                                  Authentication authentication) {
        return usuarioService.cambiarBan(id, request.baneado(), authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, authentication.getName());
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente"));
    }
}