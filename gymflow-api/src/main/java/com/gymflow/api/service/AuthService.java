package com.gymflow.api.service;

import com.gymflow.api.dto.AuthResponse;
import com.gymflow.api.dto.LoginRequest;
import com.gymflow.api.dto.RegisterRequest;
import com.gymflow.api.dto.UsuarioResponse;
import com.gymflow.api.entity.Rol;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.exception.BadRequestException;
import com.gymflow.api.repository.RolRepository;
import com.gymflow.api.repository.UsuarioRepository;
import com.gymflow.api.security.JwtService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RolRepository rolRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new BadRequestException("Ese correo ya está registrado");
        }

        // Todo registro público es ROLE_USER; el rol nunca lo decide el cliente
        Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Falta el rol ROLE_USER en la base de datos"));

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setTelefono(request.telefono());
        usuario.setRol(rolUser);
        usuarioRepository.save(usuario);

        return respuesta(usuario);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (DisabledException e) {
            throw new AccessDeniedException("Tu cuenta está suspendida. Habla con recepción.");
        }
        // Si la contraseña es incorrecta, BadCredentialsException sube hasta el
        // GlobalExceptionHandler, que responde 401.

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Correo o contraseña incorrectos"));

        return respuesta(usuario);
    }

    private AuthResponse respuesta(Usuario usuario) {
        return new AuthResponse(
                jwtService.generateToken(usuario),
                "Bearer",
                UsuarioResponse.from(usuario));
    }
}