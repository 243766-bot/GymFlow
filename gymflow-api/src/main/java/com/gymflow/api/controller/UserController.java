package com.gymflow.api.controller;

import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Requiere JWT: devuelve el perfil de quien envía el token
    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return usuarioService.perfil(authentication.getName());
    }
}