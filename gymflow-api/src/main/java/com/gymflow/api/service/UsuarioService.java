package com.gymflow.api.service;

import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.exception.NotFoundException;
import com.gymflow.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse perfil(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
}