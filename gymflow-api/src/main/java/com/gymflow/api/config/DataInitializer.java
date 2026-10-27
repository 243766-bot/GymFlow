package com.gymflow.api.config;

import com.gymflow.api.entity.Rol;
import com.gymflow.api.entity.Usuario;
import com.gymflow.api.repository.RolRepository;
import com.gymflow.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner inicializar(RolRepository rolRepository,
                                  UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.admin.email:}") String adminEmail,
                                  @Value("${app.admin.password:}") String adminPassword) {
        return args -> {
            // 1. Crear los roles si no existen
            for (String nombre : new String[]{"ROLE_ADMIN", "ROLE_USER"}) {
                if (rolRepository.findByNombre(nombre).isEmpty()) {
                    rolRepository.save(new Rol(nombre));
                }
            }

            // 2. Crear el administrador inicial si está configurado y no existe
            if (!adminEmail.isBlank() && !adminPassword.isBlank()) {
                String email = adminEmail.trim().toLowerCase();
                if (!usuarioRepository.existsByEmail(email)) {
                    Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").orElseThrow();

                    Usuario admin = new Usuario();
                    admin.setNombre("Administrador");
                    admin.setEmail(email);
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRol(rolAdmin);
                    usuarioRepository.save(admin);
                }
            }
        };
    }
}